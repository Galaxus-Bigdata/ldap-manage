import zlib
import socket
import time

# ZooKeeper server address and port
zk_host = "127.0.0.1"
zk_port = 2181

# ZooKeeper paths
active_controller_path = "/controller"
brokers_path = "/brokers/ids"

# Output file path
output_file = "active_controller_hostname.txt"

def get_active_controller_hostname():
    # Connect to ZooKeeper server
    zk_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    zk_socket.connect((zk_host, zk_port))

    # Retrieve the active controller broker ID
    request = f"OP_GET {active_controller_path}\r\n".encode()
    request_len = str(len(request)).encode() + b'\0'
    request = (request_len + request)
    zk_socket.sendall(request)
    response = recv_response(zk_socket)
    active_controller_id = response.decode().strip()

    # Retrieve the broker information
    request = f"OP_GET {brokers_path}\r\n".encode()
    request_len = str(len(request)).encode() + b'\0'
    request = (request_len + request)
    zk_socket.sendall(request)
    response = recv_response(zk_socket)

    # Parse the broker information
    brokers = {}
    for broker_info in response.decode().split('\n'):
        if broker_info:
            broker_id, hostname, _ = broker_info.split(',')
            brokers[broker_id] = hostname

    # Close the ZooKeeper connection
    zk_socket.close()

    # Return the active controller hostname
    if active_controller_id in brokers:
        return brokers[active_controller_id]
    else:
        return None

def recv_response(zk_socket):
    response = b''
    while True:
        chunk = zk_socket.recv(1024)
        if not chunk:
            break
        response += chunk
    return parse_response(response)

def parse_response(response):
    offset = 0
    response_len, = zlib.crc32(response[:4]), 0, 4
    offset += 4

    if response_len > 0:
        data_len, = zlib.crc32(response[offset:offset + 4]), 0, 4
        offset += 4

        data = response[offset:offset + data_len]
        return data
    else:
        return b''

def main():
    while True:
        active_controller_hostname = get_active_controller_hostname()
        if active_controller_hostname:
            with open(output_file, "w") as file:
                file.write(active_controller_hostname)
            print(f"Active controller hostname written to {output_file}: {active_controller_hostname}")
        else:
            print("Active controller hostname not found.")
        time.sleep(60)  # Wait for 1 minute

if __name__ == "__main__":
    main()
