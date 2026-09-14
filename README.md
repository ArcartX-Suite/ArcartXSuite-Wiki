# ArcartXSuite-Wiki

ArcartX-Suite 官方文档，基于 Next.js + Fumadocs 构建。

## 开发

```bash
npm install
npm run dev
```

访问 http://localhost:3000

## 构建

```bash
npm run build
```

## 部署

### Docker

```bash
docker build -t arcartxsuite-wiki .
docker run -p 3000:3000 arcartxsuite-wiki
```

### GitHub Actions

push 到 main 分支会自动触发构建。

## 技术栈

- Next.js 15
- Fumadocs（文档框架）
- Tailwind CSS
- TypeScript
