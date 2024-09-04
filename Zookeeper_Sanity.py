import subprocess

def check_zookeeper_health(host='localhost', port=2181):
    try:
        # Construct the echo command to send 'ruok' and pipe it to nc (netcat)
        zookeeper_command = f'echo ruok | nc {host} {port}'
        
        # Use Popen to run the command
        process = subprocess.Popen(zookeeper_command, stdout=subprocess.PIPE, stderr=subprocess.PIPE, shell=True)
        
        # Communicate to get stdout and stderr
        stdout, stderr = process.communicate()
        
        # Check the return code
        if process.returncode == 0:
            response = stdout.decode('utf-8').strip()
            if response == "imok":
                print("ZooKeeper is running fine.")
            else:
                print(f"Unexpected response from ZooKeeper: {response}")
        else:
            print("Failed to check ZooKeeper health.")
            print("Error:", stderr.decode('utf-8'))
    
    except Exception as e:
        print(f"An error occurred: {str(e)}")

# Example usage
check_zookeeper_health('localhost', 2181)
