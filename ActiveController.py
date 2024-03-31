import subprocess
import re

# ZooKeeper server address and port
zk_host = "localhost"
zk_port = "2181"

# Path to the zookeeper-shell.sh script
zk_shell_path = "/path/to/zookeeper/bin/zookeeper-shell.sh"

# Connect to ZooKeeper server
try:
    zk_shell = subprocess.Popen(
        [zk_shell_path, "-server", f"{zk_host}:{zk_port}"],
        stdin=subprocess.PIPE,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        universal_newlines=True,
    )
except Exception as e:
    print(f"Error connecting to ZooKeeper: {e}")
    exit(1)

# Get the active controller broker ID
try:
    output, _ = zk_shell.communicate(input="get /controller\n")
    active_controller_id = re.search(r"(\d+)", output).group(1)
except Exception as e:
    print(f"Error retrieving active controller ID: {e}")
    zk_shell.kill()
    exit(1)

# Get the broker information
try:
    output, _ = zk_shell.communicate(input=f"get /brokers/ids/{active_controller_id}\n")
    broker_info = re.search(r'{".*?"}', output).group()
except Exception as e:
    print(f"Error retrieving broker information: {e}")
    zk_shell.kill()
    exit(1)

# Parse the broker information
broker_data = eval(broker_info)
active_controller_hostname = broker_data["host"]

# Print the active controller hostname
print(f"Active controller hostname: {active_controller_hostname}")

# Clean up
zk_shell.kill()
