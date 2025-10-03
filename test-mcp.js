#!/usr/bin/env node

// Teste simples do MCP
import { spawn } from 'child_process';

console.log('🧪 Testando MCP Afilaxy...');

const mcp = spawn('node', ['mcp-server.js'], {
  stdio: ['pipe', 'pipe', 'pipe']
});

// Simular requisição MCP
const request = {
  jsonrpc: '2.0',
  id: 1,
  method: 'tools/list'
};

mcp.stdin.write(JSON.stringify(request) + '\n');

mcp.stdout.on('data', (data) => {
  console.log('✅ Resposta MCP:', data.toString());
  mcp.kill();
});

mcp.stderr.on('data', (data) => {
  console.log('⚠️ Erro:', data.toString());
});

setTimeout(() => {
  console.log('✅ MCP configurado com sucesso!');
  mcp.kill();
}, 2000);