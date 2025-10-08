#!/usr/bin/env node

import { spawn } from 'child_process';

class AfilaxyMCPClient {
  constructor() {
    this.requestId = 1;
  }

  async callTool(toolName, args = {}) {
    return new Promise((resolve, reject) => {
      const mcp = spawn('node', ['mcp-server.js'], {
        stdio: ['pipe', 'pipe', 'pipe']
      });

      const request = {
        jsonrpc: '2.0',
        id: this.requestId++,
        method: 'tools/call',
        params: {
          name: toolName,
          arguments: args
        }
      };

      let output = '';
      
      mcp.stdout.on('data', (data) => {
        output += data.toString();
      });

      mcp.on('close', () => {
        try {
          const response = JSON.parse(output);
          if (response.result) {
            resolve(response.result.content[0].text);
          } else {
            reject(new Error('Erro na resposta MCP'));
          }
        } catch (error) {
          reject(error);
        }
      });

      mcp.stdin.write(JSON.stringify(request) + '\n');
      mcp.stdin.end();
    });
  }

  async demonstrateCapabilities() {
    console.log('🫁 Demonstração MCP Afilaxy\n');

    try {
      console.log('1️⃣ Verificando saúde do projeto...');
      const health = await this.callTool('project_health');
      console.log(health + '\n');

      console.log('2️⃣ Status do Firebase...');
      const firebase = await this.callTool('firebase_status');
      console.log(firebase + '\n');

      console.log('3️⃣ Status do build...');
      const build = await this.callTool('build_status');
      console.log(build + '\n');

      console.log('4️⃣ Sugestões para UI...');
      const suggestions = await this.callTool('code_suggestions', { area: 'ui' });
      console.log(suggestions + '\n');

    } catch (error) {
      console.error('❌ Erro:', error.message);
    }
  }
}

// Executar demonstração se chamado diretamente
if (import.meta.url === `file://${process.argv[1]}`) {
  const client = new AfilaxyMCPClient();
  client.demonstrateCapabilities();
}

export default AfilaxyMCPClient;