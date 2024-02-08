import requests
from concurrent.futures import ThreadPoolExecutor

thread_pool = ThreadPoolExecutor(100)

proxies = {
    "http": "http://localhost:7070"
}

def make_request():
    response = requests.get("http://example.com", proxies=proxies)
    return response

r = make_request()

req_count = 1200

futures = [thread_pool.submit(make_request) for _ in range(req_count)]

for idx, f in enumerate(futures):
    print(idx, f.result().status_code)
