You can use Paramiko, a Python library for SSH, to run a Python script on a remote server and retrieve the output. Here's a basic example:

```python
import paramiko

def run_script_on_server(hostname, username, private_key_path, script_path):
    try:
        # Create an SSH client
        client = paramiko.SSHClient()
        # Automatically add the server's host key (this is insecure; see paramiko documentation)
        client.set_missing_host_key_policy(paramiko.AutoAddPolicy())

        # Load the private key
        private_key = paramiko.RSAKey(filename=private_key_path)

        # Connect to the server using key-based authentication
        client.connect(hostname, username=username, pkey=private_key)

        # Execute the Python script
        stdin, stdout, stderr = client.exec_command(f"python {script_path}")

        # Retrieve and print the output
        output = stdout.read().decode('utf-8')
        print(f"Output for {hostname}:")
        print(output)

    except Exception as e:
        print(f"Error connecting to {hostname}: {e}")

    finally:
        # Close the connection
        client.close()

def main():
    server_list = [
        {'hostname': 'your_server', 'username': 'your_username', 'private_key_path': '/path/to/your/private_key.pem', 'script_path': '/path/to/your/script.py'},
        # Add more servers as needed
    ]

    for server in server_list:
        run_script_on_server(server['hostname'], server['username'], server['private_key_path'], server['script_path'])

if __name__ == "__main__":
    main()
```

Replace placeholders like `'your_server'`, `'your_username'`, `'/path/to/your/private_key.pem'`, and `'/path/to/your/script.py'` with your actual server details, username, private key path, and script path.

Ensure that Paramiko is installed before running the script:

```bash
pip install paramiko
```

This script uses key-based authentication for better security. Make sure you have the private key file (usually a `.pem` file) for the specified user on the remote server. Adjust the Python script path and any other parameters according to your setup.
