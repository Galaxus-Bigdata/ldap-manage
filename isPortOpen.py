
Certainly! You can modify the previous script to include port checking. Here's an example:

```python
import socket

def is_port_open(hostname, port):
    try:
        with socket.create_connection((hostname, port), timeout=1):
            return True
    except (socket.error, socket.timeout):
        return False

def check_port_for_host(hostname, port):
    ip_address = get_ip_address(hostname)
    if ip_address:
        if is_port_open(ip_address, port):
            print(f"Port {port} is open for {hostname} ({ip_address})")
        else:
            print(f"Port {port} is closed for {hostname} ({ip_address})")
    else:
        print(f"Unable to resolve IP address for {hostname}")

def get_ip_address(hostname):
    try:
        ip_address = socket.gethostbyname(hostname)
        return ip_address
    except socket.error as e:
        return None

def main():
    filename = 'hostnames.txt'  # Replace with your filename
    port_to_check = 80  # Replace with your desired port

    with open(filename, 'r') as file:
        hostnames = file.read().splitlines()

    for hostname in hostnames:
        check_port_for_host(hostname, port_to_check)

if __name__ == "__main__":
    main()
```

Replace `'hostnames.txt'` with the path to your file containing the list of hostnames, one per line. This script will check if the specified port is open for each hostname in the file and print the result. Adjust the `port_to_check` variable as needed.
