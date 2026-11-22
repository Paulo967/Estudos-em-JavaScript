/* Cenário em código: caverna musgosa com um banco no centro (SVG gerado por script).
   Tudo é desenhado aqui; não depende de nenhuma imagem. Mude "seed" para variar o musgo e as pedras. */
(() => {
  const svg = document.getElementById('plano');
  let seed = 11;
  const R = () => (seed = (seed * 16807) % 2147483647) / 2147483647;
  const rnd = (a, b) => a + (b - a) * R();
  const pick = a => a[Math.floor(R() * a.length)];
  const INK = '#0a0f0b', BLACK = '#020403';
  const C = (x, y, r, f) => `<circle cx="${Math.round(x)}" cy="${Math.round(y)}" r="${Math.max(1, Math.round(r))}" fill="${f}"/>`;

  /* ---------- peças reutilizáveis ---------- */
  // conjunto de bolinhas com contorno: musgo, arbustos e montes
  const blob = (p, c) =>
    p.map(([x, y, r]) => C(x, y, r + 4, INK)).join('') +
    p.map(([x, y, r]) => C(x, y, r, c[0])).join('') +
    p.map(([x, y, r]) => C(x + r * 0.25, y + r * 0.3, r * 0.6, c[2])).join('') +
    p.map(([x, y, r]) => C(x - r * 0.3, y - r * 0.3, r * 0.3, c[1])).join('') +
    Array.from({ length: p.length * 2 }, () => {
      const [x, y, r] = pick(p);
      return C(x + rnd(-r, r) * 0.6, y + rnd(-r, r) * 0.6, rnd(2.5, 5.5), c[1]);
    }).join('');

  const musgo = (x, len, w, c) => {            // fio de musgo pendurado
    const p = [];
    for (let y = -10; y < len; y += w * 0.5)
      p.push([x + Math.sin(y / 60 + x) * w * 0.15, y, w * (1 - 0.5 * y / len) * rnd(0.85, 1.12)]);
    return blob(p, c);
  };
  const monte = (cx, cy, rx, ry, n, c) => {    // monte ou arbusto de musgo
    const p = [];
    for (let i = 0; i < n; i++) {
      const a = R() * 6.283, d = Math.sqrt(R());
      p.push([cx + Math.cos(a) * rx * d, cy + Math.sin(a) * ry * d, rx * rnd(0.2, 0.32)]);
    }
    p.sort((a, b) => a[1] - b[1]);
    return `<ellipse cx="${cx}" cy="${cy}" rx="${Math.round(rx * 0.85)}" ry="${Math.round(ry * 0.7)}" fill="${c[2]}" stroke="${INK}" stroke-width="4"/>` + blob(p, c);
  };
  const osso = (d, w, c1 = '#9fab9a', c2 = '#cfd8c6') =>
    `<path d="${d}" fill="none" stroke="${INK}" stroke-width="${w + 12}" stroke-linecap="round"/>` +
    `<path d="${d}" fill="none" stroke="${c1}" stroke-width="${w}" stroke-linecap="round"/>` +
    `<path d="${d}" fill="none" stroke="${c2}" stroke-width="${w * 0.22}" stroke-linecap="round" transform="translate(${-w * 0.18} ${-w * 0.18})"/>`;
  const peca = (d, f = '#b4bfac') => `<path d="${d}" fill="${f}" stroke="${INK}" stroke-width="7" stroke-linejoin="round"/>`;
  const folha = (x, y, rot, sc) =>
    `<path transform="translate(${x} ${y}) rotate(${rot}) scale(${sc})" d="M0 0C20 -34 66 -40 90 -12C64 18 20 24 0 0Z" fill="${BLACK}"/>`;
  const samambaia = (x0, y0, x1, y1, x2, y2, x3, y3, n) => {
    let o = `<path d="M${x0} ${y0}C${x1} ${y1} ${x2} ${y2} ${x3} ${y3}" fill="none" stroke="#1f3d17" stroke-width="5"/>`;
    for (let i = 1; i <= n; i++) {
      const t = i / (n + 1), u = 1 - t;
      const x = u * u * u * x0 + 3 * u * u * t * x1 + 3 * u * t * t * x2 + t * t * t * x3;
      const y = u * u * u * y0 + 3 * u * u * t * y1 + 3 * u * t * t * y2 + t * t * t * y3;
      const s = i % 2 ? 1 : -1, L = rnd(18, 32) * (1 - t * 0.4);
      o += `<path d="M${Math.round(x)} ${Math.round(y)}q${Math.round(s * L * 0.6)} ${Math.round(-L * 0.5)} ${Math.round(s * L)} ${Math.round(-L * 0.1)}q${Math.round(-s * L * 0.4)} ${Math.round(L * 0.4)} ${Math.round(-s * L)} ${Math.round(L * 0.1)}z" fill="${pick(['#3f7a1e', '#5a9a28', '#2e5a18'])}" stroke="${INK}" stroke-width="1.5"/>`;
    }
    return o;
  };

  const MF = ['#2f5419', '#4f7e2a', '#1e3814'];   // musgo ao fundo
  const MN = ['#4c8521', '#82b82c', '#2f5a17'];   // musgo da frente
  const MB = ['#6aa91e', '#a2d43c', '#3e7316'];   // musgo claro
  const MD = ['#2c5a18', '#4d8626', '#1a3811'];   // musgo escuro

  /* ---------- fundo e luz ---------- */
  let h = `<defs>
    <radialGradient id="luz" cx="985" cy="590" r="860" gradientUnits="userSpaceOnUse">
      <stop offset="0" stop-color="#88a46a"/><stop offset=".25" stop-color="#6b8a47"/><stop offset=".55" stop-color="#34502c"/>
      <stop offset=".8" stop-color="#15241a"/><stop offset="1" stop-color="#070b08"/></radialGradient>
    <radialGradient id="brilho" cx="985" cy="610" r="300" gradientUnits="userSpaceOnUse">
      <stop offset="0" stop-color="#e3f5a4" stop-opacity=".42"/><stop offset="1" stop-color="#e3f5a4" stop-opacity="0"/></radialGradient>
    <radialGradient id="vinheta" cx="960" cy="560" r="1100" gradientUnits="userSpaceOnUse">
      <stop offset=".55" stop-color="#000" stop-opacity="0"/><stop offset="1" stop-color="#000" stop-opacity=".8"/></radialGradient>
  </defs>
  <rect width="1919" height="1080" fill="url(#luz)"/><ellipse cx="985" cy="610" rx="340" ry="280" fill="url(#brilho)"/>`;

  // feixes de luz curvos em volta do banco
  [[330, 270, 46], [440, 360, 40], [560, 450, 52], [690, 540, 36], [820, 640, 44]].forEach(([rx, ry, w]) => {
    h += `<path d="M${985 - rx} 720A${rx} ${ry} 0 0 1 ${985 + rx} 720" fill="none" stroke="#dff2a8" stroke-opacity=".07" stroke-width="${w}" stroke-linecap="round"/>`;
  });
  // colunas escuras ao fundo
  [[690, 60, 480], [1255, 70, 440], [905, 50, 300], [1075, 50, 300]].forEach(([x, w, hh]) => {
    h += `<rect x="${x}" y="${720 - hh}" width="${w}" height="${hh}" rx="${w / 2}" fill="#0d1a10" opacity=".28"/>`;
  });

  /* ---------- musgo do fundo ---------- */
  [[760, 380, 34], [860, 300, 30], [1010, 420, 44], [1330, 260, 34], [1470, 300, 38], [1700, 320, 40], [590, 300, 30]]
    .forEach(([x, l, w]) => (h += musgo(x, l, w, MF)));

  /* ---------- ossos grandes ---------- */
  h += osso('M520 40C380 110 230 220 160 420C120 540 110 640 120 760', 64);
  h += `<ellipse cx="162" cy="420" rx="40" ry="32" fill="#a8b3a2" stroke="${INK}" stroke-width="7"/>`;
  for (let i = 0; i < 4; i++)
    h += `<rect x="${92 + (i % 2) * 8}" y="${480 + i * 72}" width="130" height="76" rx="34" fill="${i % 2 ? '#a5b3ae' : '#8d9e9d'}" stroke="${INK}" stroke-width="7"/>` +
         `<rect x="${108 + (i % 2) * 8}" y="${492 + i * 72}" width="40" height="9" rx="4" fill="#d3dcd3" opacity=".7"/>`;
  // pétalas de osso no teto
  h += peca('M440 0L650 0C620 70 560 150 500 205C480 120 450 60 440 0Z');
  h += peca('M1270 0L1480 0C1450 60 1400 120 1360 215C1330 140 1290 70 1270 0Z');
  h += peca('M1480 0L1640 0C1610 60 1560 120 1525 150C1500 100 1490 50 1480 0Z', '#a9b5a1');
  h += `<path d="M1300 40C1330 90 1350 130 1362 190" fill="none" stroke="#dfe7d6" stroke-width="8" stroke-linecap="round" opacity=".6"/>`;
  // presas e bulbos ao lado do banco
  h += peca('M470 660C430 570 430 470 525 378C505 470 520 570 565 660Z');
  h += peca('M395 700C385 560 395 470 440 395C440 480 462 580 484 700Z', '#a2ae9b');
  h += peca('M1650 760C1640 640 1690 540 1762 470C1742 560 1762 660 1792 760Z', '#7d8a80');
  h += `<ellipse cx="585" cy="690" rx="102" ry="122" fill="#9aa793" stroke="${INK}" stroke-width="7"/>
        <path d="M520 700C530 640 560 610 600 600M540 760C560 700 600 670 645 665" fill="none" stroke="${INK}" stroke-width="5" opacity=".7"/>
        <ellipse cx="1665" cy="715" rx="92" ry="72" fill="#7d8a80" stroke="${INK}" stroke-width="7"/>`;

  /* ---------- musgo da frente ---------- */
  [[985, 300, 32], [1105, 260, 30], [1225, 380, 34], [1445, 280, 32], [1580, 380, 42], [1670, 420, 38]]
    .forEach(([x, l, w]) => (h += musgo(x, l, w, MN)));
  [[330, 280, 34], [270, 170, 22], [400, 220, 26], [190, 120, 18], [560, 110, 20]]
    .forEach(([x, l, w]) => (h += musgo(x, l, w, MB)));

  /* ---------- o banco ---------- */
  const cupula = ['#3d4a40', '#6f7e69', '#3d4a40', '#6f7e69', '#3d4a40', '#6f7e69', '#1f2a21'];
  cupula.forEach((c, i) => {
    const rx = 178 - i * 22, ry = 128 - i * 15;
    h += `<path d="M${985 - rx} 712A${rx} ${ry} 0 0 1 ${985 + rx} 712Z" fill="${c}" stroke="${INK}" stroke-width="${i ? 5 : 8}"/>`;
  });
  for (let i = 0; i < 9; i++) {
    const a = rnd(3.5, 6), d = rnd(60, 170);
    h += `<ellipse cx="${Math.round(985 + Math.cos(a) * d)}" cy="${Math.round(712 + Math.sin(a) * d * 0.7)}" rx="${rnd(6, 12) | 0}" ry="4" fill="#8fc22a" opacity=".8"/>`;
  }
  h += `<path d="M815 744C805 782 810 812 842 830L1128 830C1160 812 1165 782 1155 744Z" fill="#222e25" stroke="${INK}" stroke-width="7"/>
        <path d="M830 770Q985 792 1140 770M835 797Q985 817 1135 797" fill="none" stroke="#3e4d41" stroke-width="6"/>
        <rect x="800" y="706" width="370" height="42" rx="19" fill="#53645a" stroke="${INK}" stroke-width="7"/>
        <rect x="814" y="709" width="342" height="10" rx="5" fill="#a4b497"/>
        <ellipse cx="868" cy="710" rx="22" ry="6" fill="#8fd02a"/><ellipse cx="1098" cy="710" rx="30" ry="6" fill="#8fd02a"/><ellipse cx="972" cy="711" rx="16" ry="5" fill="#8fd02a"/>`;

  /* ---------- arbustos de musgo ao lado do banco ---------- */
  h += monte(745, 725, 70, 60, 9, MB);        // lima brilhante à esquerda
  h += monte(1340, 735, 110, 55, 10, MD);
  h += monte(1620, 790, 95, 55, 9, MN);
  h += monte(275, 470, 70, 65, 8, MB);        // moita clara na lateral esquerda
  h += monte(455, 615, 40, 30, 5, MD);

  /* ---------- fileira de pedrinhas ---------- */
  const pal = ['#8c9b86', '#a4b09c', '#76876f', '#b4bfac', '#93a08c'];
  [[806, 0, 0], [844, 16, 1]].forEach(([yy, off, fundo]) => {
    for (let x = 455 + off; x < 1800; x += 33) {
      const y = yy + rnd(-7, 7) - Math.abs(x - 985) * 0.012, rx = rnd(24, 30), ry = rnd(27, 34);
      h += `<ellipse cx="${x}" cy="${Math.round(y)}" rx="${Math.round(rx)}" ry="${Math.round(ry)}" fill="${fundo ? '#667761' : pick(pal)}" stroke="${INK}" stroke-width="5"/>` +
           `<ellipse cx="${x - 7}" cy="${Math.round(y - ry * 0.45)}" rx="${Math.round(rx * 0.5)}" ry="${Math.round(ry * 0.28)}" fill="#e2e9d8" opacity=".35"/>`;
      if (R() < 0.35) h += `<ellipse cx="${x + rnd(-10, 10) | 0}" cy="${Math.round(y - ry * 0.6)}" rx="9" ry="5" fill="#7cb623"/>`;
    }
  });

  /* ---------- montes de musgo da frente ---------- */
  h += monte(1520, 900, 260, 70, 16, MN);
  h += monte(1400, 870, 120, 50, 8, MB);
  h += monte(690, 925, 220, 55, 12, MN);
  h += monte(1130, 960, 160, 40, 7, MD);

  /* ---------- silhuetas escuras da frente ---------- */
  h += `<path d="M0 700C90 690 180 740 250 745C330 750 400 780 450 830C500 880 520 960 600 1080L0 1080Z" fill="${BLACK}"/>
        <path d="M368 730C360 620 372 520 408 428C428 520 452 620 446 730Z" fill="${BLACK}"/>
        <path d="M520 900C560 780 540 680 575 560" fill="none" stroke="${BLACK}" stroke-width="7" stroke-linecap="round"/>`;
  h += folha(540, 790, -30, 0.9) + folha(552, 725, 200, 1) + folha(566, 645, -25, 1.1) + folha(572, 590, 190, 0.9) + folha(577, 548, -80, 0.9);
  h += `<path d="M0 1080L0 950C200 965 400 1015 700 1022C1000 1030 1300 1002 1500 972C1700 942 1850 905 1919 885L1919 1080Z" fill="${BLACK}"/>
        <path d="M1919 640C1870 690 1850 780 1830 880L1919 880Z" fill="${BLACK}"/>`;

  /* ---------- trepadeiras à direita ---------- */
  h += samambaia(1830, 170, 1790, 300, 1850, 450, 1800, 720, 24);
  h += samambaia(1765, 300, 1735, 420, 1775, 520, 1745, 700, 14);

  h += '<rect width="1919" height="1080" fill="url(#vinheta)"/>';
  svg.innerHTML = h;
})();