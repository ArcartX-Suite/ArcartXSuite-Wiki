import { createServer } from 'node:http';
import { readFile, stat } from 'node:fs/promises';
import { resolve, sep, extname } from 'node:path';
import { fileURLToPath } from 'node:url';

const root = fileURLToPath(new URL('../out/', import.meta.url));
const base = '/ArcartXSuite-Wiki';
const mime = { '.html': 'text/html; charset=utf-8', '.js': 'text/javascript; charset=utf-8', '.css': 'text/css; charset=utf-8', '.json': 'application/json; charset=utf-8', '.svg': 'image/svg+xml', '.png': 'image/png', '.ico': 'image/x-icon', '.woff2': 'font/woff2' };
const port = Number(process.env.PORT || 3087);
createServer(async (request, response) => {
  try {
    const pathname = decodeURIComponent(new URL(request.url, 'http://localhost').pathname);
    if (pathname === '/') { response.writeHead(302, { Location: `${base}/` }).end(); return; }
    if (pathname !== base && !pathname.startsWith(`${base}/`)) { response.writeHead(404).end(); return; }
    let file = resolve(root, `.${pathname.slice(base.length) || '/'}`);
    if (file !== resolve(root) && !file.startsWith(resolve(root) + sep)) { response.writeHead(404).end(); return; }
    if ((await stat(file)).isDirectory()) file = resolve(file, 'index.html');
    const body = await readFile(file);
    response.writeHead(200, { 'Content-Type': mime[extname(file)] || 'application/octet-stream', 'Cache-Control': 'no-store' }).end(body);
  } catch { response.writeHead(404).end(); }
}).listen(port, '127.0.0.1', () => console.log(`Local: http://127.0.0.1:${port}${base}/`));
