Certainly! You can use the `socket` module in Python to achieve this. Here's a simple script that reads hostnames from a file and prints their corresponding IP addresses:

```python
import socket

def get_ip_address(hostname):
    try:
        ip_address = socket.gethostbyname(hostname)
        return ip_address
    except socket.error as e:
        return f"Unable to resolve {hostname}: {e}"

def main():
    filename = 'hostnames.txt'  # Replace with your filename

    with open(filename, 'r') as file:
        hostnames = file.read().splitlines()

    for hostname in hostnames:
        ip_address = get_ip_address(hostname)
        print(f"{hostname}: {ip_address}")

if __name__ == "__main__":
    main()
```

Replace `'hostnames.txt'` with the path to your file containing the list of hostnames, one per line. When you run this script, it will print the corresponding IP addresses for each hostname in the file.
