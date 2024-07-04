import multiprocessing
import time

def dummy_function():
    # This function does nothing
    pass

if __name__ == "__main__":
    # Create a dummy process
    dummy_process = multiprocessing.Process(target=dummy_function)
    
    # Start the dummy process
    dummy_process.start()
    
    # Optionally, wait for a bit to ensure the process has started
    time.sleep(1)
    
    # Terminate the dummy process
    dummy_process.terminate()
    
    # Ensure the process has ended
    dummy_process.join()
    
    print("Dummy process has been created and terminated.")
