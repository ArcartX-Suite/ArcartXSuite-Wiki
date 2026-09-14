import { createMDX } from 'fumadocs-mdx/next';

const withMDX = createMDX();

const basePath = '/ArcartXSuite-Wiki';

/** @type {import('next').NextConfig} */
const config = {
  reactStrictMode: true,
  output: 'export',
  images: {
    unoptimized: true,
  },
  basePath,
  assetPrefix: basePath + '/',
  env: {
    NEXT_PUBLIC_BASE_PATH: basePath,
  },
};

export default withMDX(config);
