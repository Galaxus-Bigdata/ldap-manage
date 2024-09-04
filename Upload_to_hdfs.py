import subprocess

def upload_to_hdfs(local_file_path, hdfs_directory):
    try:
        # Construct the HDFS command
        hdfs_command = ['hdfs', 'dfs', '-put', local_file_path, hdfs_directory]

        # Use Popen to run the command
        process = subprocess.Popen(hdfs_command, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        
        # Communicate to get stdout and stderr
        stdout, stderr = process.communicate()
        
        # Check the return code
        if process.returncode == 0:
            print("File uploaded successfully to HDFS.")
            print("Output:", stdout.decode('utf-8'))
        else:
            print("Failed to upload file to HDFS.")
            print("Error:", stderr.decode('utf-8'))
    
    except Exception as e:
        print(f"An error occurred: {str(e)}")

# Example usage
local_file = "/path/to/local/file.txt"
hdfs_dir = "/path/to/hdfs/directory/"

upload_to_hdfs(local_file, hdfs_dir)
