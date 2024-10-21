import os
import shutil

def get_directory_size(directory):
    total_size = 0
    for dirpath, dirnames, filenames in os.walk(directory):
        for f in filenames:
            fp = os.path.join(dirpath, f)
            total_size += os.path.getsize(fp)
    return total_size

def delete_log_file(filepath):
    if os.path.exists(filepath):
        os.remove(filepath)
        print(f"Deleted log file: {filepath}")
    else:
        print(f"Log file not found: {filepath}")

def check_and_cleanup_logs(log_directory, log_file_to_delete, size_limit_gb=1):
    # Get the size of the log directory in bytes
    dir_size = get_directory_size(log_directory)
    # Convert size limit to bytes (1 GB = 1 * 1024 * 1024 * 1024 bytes)
    size_limit_bytes = size_limit_gb * 1024 * 1024 * 1024

    if dir_size > size_limit_bytes:
        print(f"Log directory size exceeds {size_limit_gb} GB. Current size: {dir_size / (1024 * 1024):.2f} MB")
        # Delete the specific log file
        delete_log_file(os.path.join(log_directory, log_file_to_delete))
    else:
        print(f"Log directory size is within the limit. Current size: {dir_size / (1024 * 1024):.2f} MB")

# Example usage
log_directory = "/path/to/log/directory"
log_file_to_delete = "specific_log_file.log"

check_and_cleanup_logs(log_directory, log_file_to_delete, size_limit_gb=1)
