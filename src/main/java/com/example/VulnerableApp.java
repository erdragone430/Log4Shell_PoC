package com.example;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class VulnerableApp {
    private static final Logger logger = LogManager.getLogger(VulnerableApp.class);
    private static final Set<String> registeredUsers = new HashSet<>(Arrays.asList("admin", "user", "test"));

    // Helper method for Java 8 compatibility - read all bytes from InputStream
    private static byte[] readAllBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[4096];
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }

    public static void main(String[] args) throws Exception {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        
        server.createContext("/", new RootHandler());
        server.createContext("/register", new RegisterHandler());
        server.createContext("/login", new LoginHandler());
        server.setExecutor(null);
        server.start();
        
        logger.info("Vulnerable Log4Shell server started on port " + port);
        System.out.println("╔════════════════════════════════════════════════════╗");
        System.out.println("║  Log4Shell Vulnerable Web Application             ║");
        System.out.println("║  Educational PoC - CVE-2021-44228                  ║");
        System.out.println("╚════════════════════════════════════════════════════╝");
        System.out.println();
    }

    static class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "<!DOCTYPE html>\n" +
                    "<html lang='it'>\n" +
                    "<head>\n" +
                    "    <meta charset='UTF-8'>\n" +
                    "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>\n" +
                    "    <title>UserApp - Registrazione</title>\n" +
                    "    <style>\n" +
                    "        * { margin: 0; padding: 0; box-sizing: border-box; }\n" +
                    "        body { font-family: 'Segoe UI', Tahoma, sans-serif; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); min-height: 100vh; display: flex; justify-content: center; align-items: center; padding: 20px; }\n" +
                    "        .container { background: white; border-radius: 20px; box-shadow: 0 20px 60px rgba(0,0,0,0.3); max-width: 450px; width: 100%; padding: 40px; }\n" +
                    "        h1 { color: #333; margin-bottom: 10px; font-size: 28px; }\n" +
                    "        .subtitle { color: #666; margin-bottom: 30px; font-size: 14px; }\n" +
                    "        .tabs { display: flex; margin-bottom: 30px; border-bottom: 2px solid #eee; }\n" +
                    "        .tab { flex: 1; padding: 12px; text-align: center; cursor: pointer; color: #666; font-weight: 500; border-bottom: 3px solid transparent; transition: all 0.3s; }\n" +
                    "        .tab.active { color: #667eea; border-bottom-color: #667eea; }\n" +
                    "        .tab:hover { background: #f5f5f5; }\n" +
                    "        .form-group { margin-bottom: 20px; }\n" +
                    "        label { display: block; margin-bottom: 8px; color: #555; font-weight: 500; font-size: 14px; }\n" +
                    "        input { width: 100%; padding: 12px 15px; border: 2px solid #e0e0e0; border-radius: 8px; font-size: 15px; transition: border-color 0.3s; }\n" +
                    "        input:focus { outline: none; border-color: #667eea; }\n" +
                    "        button { width: 100%; padding: 14px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; border: none; border-radius: 8px; font-size: 16px; font-weight: 600; cursor: pointer; transition: transform 0.2s, box-shadow 0.2s; }\n" +
                    "        button:hover { transform: translateY(-2px); box-shadow: 0 5px 20px rgba(102, 126, 234, 0.4); }\n" +
                    "        button:active { transform: translateY(0); }\n" +
                    "        .warning { background: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin-top: 30px; border-radius: 5px; font-size: 13px; color: #856404; }\n" +
                    "        .warning strong { display: block; margin-bottom: 5px; }\n" +
                    "        #message { margin-top: 15px; padding: 12px; border-radius: 8px; display: none; font-size: 14px; }\n" +
                    "        .error { background: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; display: block; }\n" +
                    "        .success { background: #d4edda; color: #155724; border: 1px solid #c3e6cb; display: block; }\n" +
                    "        .info-box { background: #e7f3ff; border: 1px solid #b3d9ff; padding: 15px; border-radius: 8px; margin-top: 20px; font-size: 13px; color: #004085; }\n" +
                    "        .info-box strong { display: block; margin-bottom: 8px; font-size: 14px; }\n" +
                    "        .info-box code { background: #fff; padding: 2px 6px; border-radius: 3px; font-family: monospace; color: #d63384; }\n" +
                    "    </style>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "    <div class='container'>\n" +
                    "        <h1>UserApp</h1>\n" +
                    "        <p class='subtitle'>User mmanagement system</p>\n" +
                    "        \n" +
                    "        <div class='tabs'>\n" +
                    "            <div class='tab active' onclick='showTab(\"register\")'>Registrazione</div>\n" +
                    "            <div class='tab' onclick='showTab(\"login\")'>Login</div>\n" +
                    "        </div>\n" +
                    "        \n" +
                    "        <form id='registerForm' action='/register' method='POST'>\n" +
                    "            <div class='form-group'>\n" +
                    "                <label for='username'>Username</label>\n" +
                    "                <input type='text' id='username' name='username' required placeholder='Scegli un username'>\n" +
                    "            </div>\n" +
                    "            <div class='form-group'>\n" +
                    "                <label for='email'>Email</label>\n" +
                    "                <input type='email' id='email' name='email' required placeholder='tua@email.com'>\n" +
                    "            </div>\n" +
                    "            <div class='form-group'>\n" +
                    "                <label for='password'>Password</label>\n" +
                    "                <input type='password' id='password' name='password' required placeholder='Minimo 6 caratteri'>\n" +
                    "            </div>\n" +
                    "            <button type='submit'>Registrati</button>\n" +
                    "        </form>\n" +
                    "        \n" +
                    "        <form id='loginForm' action='/login' method='POST' style='display:none;'>\n" +
                    "            <div class='form-group'>\n" +
                    "                <label for='login_username'>Username</label>\n" +
                    "                <input type='text' id='login_username' name='username' required placeholder='Il tuo username'>\n" +
                    "            </div>\n" +
                    "            <div class='form-group'>\n" +
                    "                <label for='login_password'>Password</label>\n" +
                    "                <input type='password' id='login_password' name='password' required placeholder='La tua password'>\n" +
                    "            </div>\n" +
                    "            <button type='submit'>Accedi</button>\n" +
                    "        </form>\n" +
                    "        \n" +
                    "        <div id='message'></div>\n" +
                    "        \n" +
                    "        <div class='warning'>\n" +
                    "            <strong>WARNING</strong>\n" +
                    "            This application is intentionally vulnerable for educational purposes. Do not use in production!\n" +
                    "        </div>\n" +
                    "    </div>\n" +
                    "    \n" +
                    "    <script>\n" +
                    "        function showTab(tab) {\n" +
                    "            const tabs = document.querySelectorAll('.tab');\n" +
                    "            tabs.forEach(t => t.classList.remove('active'));\n" +
                    "            event.target.classList.add('active');\n" +
                    "            \n" +
                    "            if (tab === 'register') {\n" +
                    "                document.getElementById('registerForm').style.display = 'block';\n" +
                    "                document.getElementById('loginForm').style.display = 'none';\n" +
                    "            } else {\n" +
                    "                document.getElementById('registerForm').style.display = 'none';\n" +
                    "                document.getElementById('loginForm').style.display = 'block';\n" +
                    "            }\n" +
                    "            document.getElementById('message').style.display = 'none';\n" +
                    "        }\n" +
                    "        \n" +
                    "        document.getElementById('registerForm').addEventListener('submit', async (e) => {\n" +
                    "            e.preventDefault();\n" +
                    "            const formData = new FormData(e.target);\n" +
                    "            const data = Object.fromEntries(formData);\n" +
                    "            \n" +
                    "            const response = await fetch('/register', {\n" +
                    "                method: 'POST',\n" +
                    "                headers: { 'Content-Type': 'application/json' },\n" +
                    "                body: JSON.stringify(data)\n" +
                    "            });\n" +
                    "            \n" +
                    "            const result = await response.json();\n" +
                    "            const messageDiv = document.getElementById('message');\n" +
                    "            messageDiv.textContent = result.message;\n" +
                    "            messageDiv.className = result.success ? 'success' : 'error';\n" +
                    "            messageDiv.style.display = 'block';\n" +
                    "        });\n" +
                    "        \n" +
                    "        document.getElementById('loginForm').addEventListener('submit', async (e) => {\n" +
                    "            e.preventDefault();\n" +
                    "            const formData = new FormData(e.target);\n" +
                    "            const data = Object.fromEntries(formData);\n" +
                    "            \n" +
                    "            const response = await fetch('/login', {\n" +
                    "                method: 'POST',\n" +
                    "                headers: { 'Content-Type': 'application/json' },\n" +
                    "                body: JSON.stringify(data)\n" +
                    "            });\n" +
                    "            \n" +
                    "            const result = await response.json();\n" +
                    "            const messageDiv = document.getElementById('message');\n" +
                    "            messageDiv.textContent = result.message;\n" +
                    "            messageDiv.className = result.success ? 'success' : 'error';\n" +
                    "            messageDiv.style.display = 'block';\n" +
                    "        });\n" +
                    "    </script>\n" +
                    "</body>\n" +
                    "</html>";
            
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes(StandardCharsets.UTF_8));
            os.close();
        }
    }

    static class RegisterHandler implements HttpHandler {
        private static final Logger logger = LogManager.getLogger(RegisterHandler.class);

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, false, "Method not allowed");
                return;
            }

            String body = new String(readAllBytes(exchange.getRequestBody()), StandardCharsets.UTF_8);
            Map<String, String> data = parseJSON(body);
            
            String username = data.getOrDefault("username", "");
            String email = data.getOrDefault("email", "");
            String password = data.getOrDefault("password", "");

            // Validation - Errors are logged (VULNERABILITY!)
            
            // Check 1: Username already exists - VULNERABLE!
            if (registeredUsers.contains(username)) {
                // VULNERABLE LOG: logs user input without sanitization
                logger.error("Registration failed: Username '" + username + "' already exists");
                logger.warn("Failed registration attempt with email: " + email);
                sendResponse(exchange, 400, false, "Errore: Username già in uso!");
                return;
            }

            // Check 2: Email validation - VULNERABLE!
            if (!email.contains("@") || !email.contains(".")) {
                // VULNERABLE LOG: logs invalid email
                logger.error("Registration failed: Invalid email format provided: " + email);
                logger.info("Registration attempt with username: " + username + " and invalid email: " + email);
                sendResponse(exchange, 400, false, "Errore: Formato email non valido!");
                return;
            }

            // Check 3: Password validation - VULNERABLE!
            if (password.length() < 6) {
                // VULNERABLE LOG: logs attempts with weak password
                logger.error("Registration failed: Password too short for user '" + username + "' with email: " + email);
                sendResponse(exchange, 400, false, "Errore: Password troppo corta (minimo 6 caratteri)!");
                return;
            }

            // Successful registration
            registeredUsers.add(username);
            logger.info("✓ New user registered successfully: " + username);
            sendResponse(exchange, 200, true, "✓ Registrazione completata con successo!");
        }

        private Map<String, String> parseJSON(String json) {
            Map<String, String> map = new HashMap<>();
            // Remove only outer braces
            json = json.trim();
            if (json.startsWith("{")) json = json.substring(1);
            if (json.endsWith("}")) json = json.substring(0, json.length() - 1);
            
            // Parse key-value pairs while preserving inner content
            boolean inQuotes = false;
            StringBuilder current = new StringBuilder();
            for (char c : json.toCharArray()) {
                if (c == '"' && (current.length() == 0 || current.charAt(current.length() - 1) != '\\')) {
                    inQuotes = !inQuotes;
                } else if (c == ',' && !inQuotes) {
                    parsePair(current.toString(), map);
                    current = new StringBuilder();
                } else {
                    current.append(c);
                }
            }
            if (current.length() > 0) {
                parsePair(current.toString(), map);
            }
            return map;
        }
        
        private void parsePair(String pair, Map<String, String> map) {
            int colonIndex = pair.indexOf(':');
            if (colonIndex > 0) {
                String key = pair.substring(0, colonIndex).trim().replace("\"", "");
                String value = pair.substring(colonIndex + 1).trim();
                // Remove quotes only from start and end of value
                if (value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }
                map.put(key, value);
            }
        }

        private void sendResponse(HttpExchange exchange, int statusCode, boolean success, String message) throws IOException {
            String response = "{\"success\": " + success + ", \"message\": \"" + message + "\"}";
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(statusCode, response.getBytes(StandardCharsets.UTF_8).length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes(StandardCharsets.UTF_8));
            os.close();
        }
    }

    static class LoginHandler implements HttpHandler {
        private static final Logger logger = LogManager.getLogger(LoginHandler.class);

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, false, "Method not allowed");
                return;
            }

            String body = new String(readAllBytes(exchange.getRequestBody()), StandardCharsets.UTF_8);
            Map<String, String> data = parseJSON(body);
            
            String username = data.getOrDefault("username", "");
            String password = data.getOrDefault("password", "");

            // ⚠️ VULNERABLE LOG: logs failed login attempt
            if (!registeredUsers.contains(username)) {
                logger.error("Login failed: User not found: " + username);
                sendResponse(exchange, 401, false, "Username o password wrong!");
                return;
            }

            // Simulate successful login
            logger.info("✓ Successful login for user: " + username);
            sendResponse(exchange, 200, true, "✓ Login success!");
        }

        private Map<String, String> parseJSON(String json) {
            Map<String, String> map = new HashMap<>();
            // Remove only outer braces
            json = json.trim();
            if (json.startsWith("{")) json = json.substring(1);
            if (json.endsWith("}")) json = json.substring(0, json.length() - 1);
            
            // Parse key-value pairs while preserving inner content
            boolean inQuotes = false;
            StringBuilder current = new StringBuilder();
            for (char c : json.toCharArray()) {
                if (c == '"' && (current.length() == 0 || current.charAt(current.length() - 1) != '\\')) {
                    inQuotes = !inQuotes;
                } else if (c == ',' && !inQuotes) {
                    parsePair(current.toString(), map);
                    current = new StringBuilder();
                } else {
                    current.append(c);
                }
            }
            if (current.length() > 0) {
                parsePair(current.toString(), map);
            }
            return map;
        }
        
        private void parsePair(String pair, Map<String, String> map) {
            int colonIndex = pair.indexOf(':');
            if (colonIndex > 0) {
                String key = pair.substring(0, colonIndex).trim().replace("\"", "");
                String value = pair.substring(colonIndex + 1).trim();
                // Remove quotes only from start and end of value
                if (value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }
                map.put(key, value);
            }
        }

        private void sendResponse(HttpExchange exchange, int statusCode, boolean success, String message) throws IOException {
            String response = "{\"success\": " + success + ", \"message\": \"" + message + "\"}";
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(statusCode, response.getBytes(StandardCharsets.UTF_8).length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes(StandardCharsets.UTF_8));
            os.close();
        }
    }
}
