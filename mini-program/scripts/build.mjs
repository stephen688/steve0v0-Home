import { existsSync, mkdirSync, readdirSync, rmSync, copyFileSync, utimesSync, writeFileSync } from 'node:fs';
import { dirname, join, relative, resolve } from 'node:path';
import { spawnSync } from 'node:child_process';
import { deflateSync } from 'node:zlib';
import { fileURLToPath } from 'node:url';

const root = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const dist = join(root, 'dist');
const buildDir = join(root, '.dist-build');
const buildTime = new Date();
const tsc = join(root, '..', 'web-admin', 'node_modules', 'typescript', 'bin', 'tsc');

if (!existsSync(tsc)) {
  console.error(`TypeScript compiler not found: ${tsc}`);
  process.exit(1);
}

rmSync(buildDir, { recursive: true, force: true });
mkdirSync(buildDir, { recursive: true });
mkdirSync(dist, { recursive: true });

const compiled = spawnSync(process.execPath, [tsc, '-p', join(root, 'tsconfig.json'), '--outDir', buildDir, '--rootDir', root], { stdio: 'inherit' });
if (compiled.status !== 0) process.exit(compiled.status || 1);

function copyStatic(sourceDir) {
  for (const entry of readdirSync(sourceDir, { withFileTypes: true })) {
    const source = join(sourceDir, entry.name);
    const target = join(buildDir, relative(root, source));
    if (entry.name === 'dist' || entry.name === '.dist-build' || entry.name === 'node_modules' || entry.name === 'scripts' || entry.name === 'tsconfig.json' || entry.name === 'package.json') continue;
    if (entry.isDirectory()) {
      mkdirSync(target, { recursive: true });
      copyStatic(source);
    } else if (/\.(json|wxml|wxss|png|jpg|jpeg|webp)$/i.test(entry.name)) {
      mkdirSync(dirname(target), { recursive: true });
      copyFileSync(source, target);
    }
  }
}

copyStatic(root);

const buildProjectConfig = join(buildDir, 'project.config.json');
if (existsSync(buildProjectConfig)) {
  const projectConfig = JSON.parse(await import('node:fs/promises').then((module) => module.readFile(buildProjectConfig, 'utf8')));
  projectConfig.miniprogramRoot = './';
  writeFileSync(buildProjectConfig, `${JSON.stringify(projectConfig, null, 2)}\n`);
}

function crc32(buffer) {
  let crc = 0xffffffff;
  for (const byte of buffer) {
    crc ^= byte;
    for (let bit = 0; bit < 8; bit += 1) crc = (crc >>> 1) ^ ((crc & 1) ? 0xedb88320 : 0);
  }
  return (crc ^ 0xffffffff) >>> 0;
}

function chunk(type, data) {
  const typeBuffer = Buffer.from(type);
  const length = Buffer.alloc(4);
  length.writeUInt32BE(data.length);
  const crc = Buffer.alloc(4);
  crc.writeUInt32BE(crc32(Buffer.concat([typeBuffer, data])));
  return Buffer.concat([length, typeBuffer, data, crc]);
}

function png(width, height, pixels) {
  const rows = [];
  for (let y = 0; y < height; y += 1) rows.push(Buffer.concat([Buffer.from([0]), pixels.subarray(y * width * 4, (y + 1) * width * 4)]));
  const header = Buffer.alloc(13);
  header.writeUInt32BE(width, 0);
  header.writeUInt32BE(height, 4);
  header[8] = 8;
  header[9] = 6;
  return Buffer.concat([Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]), chunk('IHDR', header), chunk('IDAT', deflateSync(Buffer.concat(rows))), chunk('IEND', Buffer.alloc(0))]);
}

function icon(kind, color) {
  const width = 81;
  const height = 81;
  const pixels = Buffer.alloc(width * height * 4);
  const put = (x, y, alpha = 255) => {
    if (x < 0 || y < 0 || x >= width || y >= height) return;
    const offset = (y * width + x) * 4;
    pixels[offset] = color[0]; pixels[offset + 1] = color[1]; pixels[offset + 2] = color[2]; pixels[offset + 3] = alpha;
  };
  const line = (x1, y1, x2, y2, thickness = 4) => {
    const steps = Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1));
    for (let step = 0; step <= steps; step += 1) {
      const x = Math.round(x1 + (x2 - x1) * step / Math.max(steps, 1));
      const y = Math.round(y1 + (y2 - y1) * step / Math.max(steps, 1));
      for (let dx = -thickness; dx <= thickness; dx += 1) for (let dy = -thickness; dy <= thickness; dy += 1) if (dx * dx + dy * dy <= thickness * thickness) put(x + dx, y + dy);
    }
  };
  const circle = (centerX, centerY, radius, thickness = 3) => {
    let previousX = centerX + radius;
    let previousY = centerY;
    for (let step = 1; step <= 48; step += 1) {
      const angle = Math.PI * 2 * step / 48;
      const x = centerX + Math.cos(angle) * radius;
      const y = centerY + Math.sin(angle) * radius;
      line(previousX, previousY, x, y, thickness);
      previousX = x;
      previousY = y;
    }
  };
  const quadratic = (startX, startY, controlX, controlY, endX, endY, thickness = 3) => {
    let previousX = startX;
    let previousY = startY;
    for (let step = 1; step <= 32; step += 1) {
      const progress = step / 32;
      const inverse = 1 - progress;
      const x = inverse * inverse * startX + 2 * inverse * progress * controlX + progress * progress * endX;
      const y = inverse * inverse * startY + 2 * inverse * progress * controlY + progress * progress * endY;
      line(previousX, previousY, x, y, thickness);
      previousX = x;
      previousY = y;
    }
  };
  if (kind === 'home') {
    line(15, 38, 40, 17, 3); line(40, 17, 66, 38, 3); line(22, 34, 22, 64, 3); line(58, 34, 58, 64, 3); line(22, 64, 58, 64, 3); line(34, 64, 34, 48, 3); line(34, 48, 46, 48, 3); line(46, 48, 46, 64, 3);
  } else if (kind === 'blog') {
    line(22, 15, 58, 15, 3); line(22, 15, 22, 66, 3); line(58, 15, 58, 66, 3); line(22, 66, 58, 66, 3); line(30, 28, 50, 28, 3); line(30, 39, 50, 39, 3); line(30, 50, 45, 50, 3);
  } else if (kind === 'moments') {
    line(13, 45, 25, 45, 3); line(25, 45, 31, 27, 3); line(31, 27, 39, 57, 3); line(39, 57, 48, 35, 3); line(48, 35, 55, 45, 3); line(55, 45, 68, 45, 3);
  } else {
    circle(40, 21, 11, 3);
    quadratic(19, 65, 20, 43, 40, 43, 3);
    quadratic(40, 43, 60, 43, 61, 65, 3);
  }
  return png(width, height, pixels);
}

const tabDir = join(buildDir, 'assets', 'tab');
mkdirSync(tabDir, { recursive: true });
const colors = { normal: [154, 149, 143], active: [244, 91, 18] };
for (const kind of ['home', 'blog', 'moments', 'about']) {
  writeFileSync(join(tabDir, `${kind}.png`), icon(kind, colors.normal));
  writeFileSync(join(tabDir, `${kind}-active.png`), icon(kind, colors.active));
}

function syncBuild(sourceDir, targetDir) {
  mkdirSync(targetDir, { recursive: true });
  const sourceEntries = readdirSync(sourceDir, { withFileTypes: true });
  const sourceNames = new Set(sourceEntries.map((entry) => entry.name));

  for (const targetEntry of readdirSync(targetDir, { withFileTypes: true })) {
    if (!sourceNames.has(targetEntry.name)) rmSync(join(targetDir, targetEntry.name), { recursive: true, force: true });
  }

  for (const sourceEntry of sourceEntries) {
    const source = join(sourceDir, sourceEntry.name);
    const target = join(targetDir, sourceEntry.name);
    if (sourceEntry.isDirectory()) syncBuild(source, target);
    else {
      copyFileSync(source, target);
      utimesSync(target, buildTime, buildTime);
    }
  }
}

syncBuild(buildDir, dist);
rmSync(buildDir, { recursive: true, force: true });

console.log(`Built mini-program to ${dist}`);
