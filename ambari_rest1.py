from flask import Flask, request
import subprocess

app = Flask(__name__)

@app.route('/get-rm-state', methods=['POST'])
def get_rm_state():
    # Get the hostname from the POST request
    hostname = request.form.get('hostname')

    if not hostname:
        return 'Hostname not provided', 400

    # Construct the Resource Manager API URL
    rm_api_url = f'https://{hostname}:8088/ws/v1/cluster/info'

    try:
        # Set up Kerberos authentication
        kinit_path = '/path/to/kinit'  # Replace with the actual path to kinit
        subprocess.run([kinit_path, '-kt', '/path/to/keytab', 'username@REALM.COM'])

        # Construct the curl command with Kerberos and SSL authentication
        curl_cmd = [
            'curl',
            '--negotiate',
            '-u', ':',
            '--cacert', '/path/to/ca.pem',
            '--cert', '/path/to/client.crt',
            '--key', '/path/to/client.key',
            rm_api_url
        ]

        # Execute the curl command and capture the output
        result = subprocess.run(curl_cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE)

        if result.returncode == 0:
            # Parse the response JSON
            cluster_info = result.stdout.decode().strip()

            # Check if the Resource Manager is Active or Standby
            rm_state = cluster_info['clusterInfo']['haState']

            return f'Resource Manager state: {rm_state}', 200
        else:
            return f'Error: {result.stderr.decode().strip()}', 500

    except subprocess.CalledProcessError as e:
        return f'Error: {e}', 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
