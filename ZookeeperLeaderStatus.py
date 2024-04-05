import socket

def get_zookeeper_mode(host, port):
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    
    try:
        # Connect to ZooKeeper
        sock.connect((host, port))
        
        # Send "srvr" command
        sock.send(b"srvr\n")
        
        # Receive response
        response = sock.recv(4096).decode("utf-8")
        
        # Parse response to get the mode
        lines = response.split("\n")
        for line in lines:
            if line.startswith("Mode:"):
                return line.split(":")[1].strip()
    finally:
        sock.close()

if __name__ == "__main__":
    # Replace with your ZooKeeper host and port
    zk_host = "localhost"
    zk_port = 2181
    
    mode = get_zookeeper_mode(zk_host, zk_port)
    print("ZooKeeper mode:", mode)
