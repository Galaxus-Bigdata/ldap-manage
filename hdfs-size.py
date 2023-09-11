import subprocess

# Specify the HDFS directory path
hdfs_directory_path = "/path/to/hdfs/directory"

# Run the Hadoop hdfs command to get the size
command = f"hadoop fs -du -s -h {hdfs_directory_path}"
result = subprocess.check_output(command, shell=True).decode('utf-8')

# Extract the size from the result
directory_size_str = result.strip().split()[0]

# Print the size
print(f"Size of {hdfs_directory_path}: {directory_size_str}")
