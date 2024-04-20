import logging
import logging.handlers

LOG_FILENAME = 'example.log'
LOG_MAX_SIZE = 256 * 1024 * 1024  # 256 MB

# Set up a rotating log
handler = logging.handlers.RotatingFileHandler(
              LOG_FILENAME, maxBytes=LOG_MAX_SIZE, backupCount=5)
formatter = logging.Formatter('%(asctime)s - %(levelname)s - %(message)s')
handler.setFormatter(formatter)

# Add the handler to the root logger
logging.getLogger().addHandler(handler)

# Example usage
logging.debug('This is a debug message')
logging.info('This is an info message')
logging.warning('This is a warning message')
logging.error('This is an error message')
logging.critical('This is a critical message')
