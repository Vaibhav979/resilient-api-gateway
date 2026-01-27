## Windows + Docker Desktop (WSL2)

- On Windows with Docker Desktop (WSL2), services are exposed via the WSL virtual adapter, not localhost.
- Find the IP:
  ipconfig → vEthernet (WSL)
- Example:
  curl http://172.17.224.1:8080/api/data

