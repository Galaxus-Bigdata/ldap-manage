import random

def generate_sample_data(num_records):
    sample_data = []
    for _ in range(num_records):
        sr_returned_date_sk = random.randint(1, 10)
        sr_customer_sk = random.randint(1, 10)
        sr_store_sk = random.randint(1, 10)
        sr_return_amt = round(random.uniform(1, 100), 2)
        sample_data.append((sr_returned_date_sk, sr_customer_sk, sr_store_sk, sr_return_amt))
    return sample_data

def insert_data_into_hive(sample_data):
    for row in sample_data:
        insert_statement = f"INSERT INTO store_returns VALUES ({row[0]}, {row[1]}, {row[2]}, {row[3]});"
        print(insert_statement)

if __name__ == "__main__":
    num_records = 100
    sample_data = generate_sample_data(num_records)
    insert_data_into_hive(sample_data)
