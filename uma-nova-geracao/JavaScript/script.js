const $=s=>document.querySelector(s),$$=s=>[...document.querySelectorAll(s)];

/* ===== Regras do jogo (ajuste aqui) ===== */
const CFG={
  anosParaVencer:50,
  segundosPorAno:8,                                   // a cada 8 s passa 1 ano
  inicial:{fome:20,felicidade:80,cansaco:0,idade:0},
  tempo:{fome:3,felicidade:-3,cansaco:10,idade:1},    // efeito de cada passagem de tempo
  alimentar:{fome:-25,felicidade:3,cansaco:2},
  brincar:{fome:5,felicidade:15,cansaco:8},
  descansar:{fome:4,cansaco:-35}
};
const CHAVE='furao-banco-save-v1';
let S=null,rodando=false,ocupado=false,acum=0,humorAntes='feliz',tFala;

/* ===== Vagalumes ===== */
for(let i=0;i<9;i++){const v=document.createElement('i');v.className='vagalume';
  v.style.cssText=`left:${25+Math.random()*50}%;top:${10+Math.random()*50}%;--x:${(Math.random()-.5)*60}px;--y:${(Math.random()-.5)*60}px;--d:${3+Math.random()*4}s;animation-delay:-${Math.random()*5}s`;
  $('#cena').append(v)}

/* ===== Objetos que aparecem nas ações ===== */
const PROPS={
  alimentar:`<svg viewBox="0 0 80 60"><ellipse cx="40" cy="54" rx="32" ry="5" fill="rgba(0,0,0,.4)"/><circle cx="26" cy="24" r="7" fill="#a8743f" stroke="#0b100c" stroke-width="3"/><circle cx="40" cy="20" r="7" fill="#8c5d31" stroke="#0b100c" stroke-width="3"/><circle cx="54" cy="24" r="7" fill="#a8743f" stroke="#0b100c" stroke-width="3"/><path d="M8 28h64c0 16-12 24-32 24S8 44 8 28z" fill="#6f7a66" stroke="#0b100c" stroke-width="4" stroke-linejoin="round"/></svg>`,
  brincar:`<svg viewBox="0 0 80 60"><ellipse cx="40" cy="54" rx="26" ry="5" fill="rgba(0,0,0,.4)"/><circle cx="40" cy="32" r="22" fill="#c43d3d" stroke="#0b100c" stroke-width="4"/><path d="M22 24c10 2 18 10 20 24M20 36c6 1 12 5 15 12M30 12c10 2 16 8 20 18" fill="none" stroke="#7a1f1f" stroke-width="3" stroke-linecap="round"/></svg>`
};

/* ===== Estado, salvar, carregar ===== */
const salvar=()=>{try{S&&!S.fim&&localStorage.setItem(CHAVE,JSON.stringify(S))}catch(e){}};
const carregar=()=>{try{const s=JSON.parse(localStorage.getItem(CHAVE));return s&&s.nome&&!s.fim?s:null}catch(e){return null}};
const apagar=()=>{try{localStorage.removeItem(CHAVE)}catch(e){}};
const vibra=n=>navigator.vibrate&&navigator.vibrate(n);
const lim=v=>Math.max(0,Math.min(100,v));

function humor(){
  if(S.fome>=70)return'faminto';
  if(S.cansaco>=70)return'cansado';
  if(S.felicidade<=30)return'triste';
  return'feliz';
}
const FALAS={faminto:'Estou com fome…',cansado:'Preciso dormir…',triste:'Brinca comigo?'};

function falar(t){const e=$('#fala');e.textContent=t;e.classList.add('v');clearTimeout(tFala);tFala=setTimeout(()=>e.classList.remove('v'),2200)}

function render(){
  $('#hNome').textContent=S.nome;
  $('#hIdade').textContent=`${S.idade} / ${CFG.anosParaVencer} anos`;
  $('#fio').style.width=(S.idade/CFG.anosParaVencer*100)+'%';
  for(const k of['fome','felicidade','cansaco']){
    const b=$(`.barra[data-k=${k}]`);
    b.querySelector('b').style.width=S[k]+'%';
    b.classList.toggle('perigo',k==='felicidade'?S[k]<=25:S[k]>=75);
  }
  const h=humor();
  if(!ocupado&&!S.fim){$('#furao').dataset.e=h;if(h!==humorAntes&&FALAS[h])falar(FALAS[h])}
  humorAntes=h;
  salvar();
}

function aplicar(d){
  for(const k of['fome','felicidade','cansaco'])if(d[k])S[k]=lim(S[k]+d[k]);
  if(d.idade)S.idade+=d.idade;
  render();checarFim();
}

function checarFim(){
  const n=S.nome;let t=null,ok=false;
  if(S.fome>=100)t=`${n} passou fome demais.`;
  else if(S.cansaco>=100)t=`${n} chegou ao limite do cansaço.`;
  else if(S.felicidade<=0)t=`${n} perdeu toda a alegria.`;
  else if(S.idade>=CFG.anosParaVencer){ok=true;t=`${n} chegou aos ${CFG.anosParaVencer} anos, cuidado até o fim.`}
  if(!t)return;
  S.fim=true;rodando=false;apagar();
  $('#furao').dataset.e=ok?'feliz':'triste';
  $('#fimTitulo').textContent=ok?'Uma vida inteira':'Fim da jornada';
  $('#fimTexto').textContent=t+(ok?'':` Viveu ${S.idade} ${S.idade===1?'ano':'anos'}.`);
  setTimeout(()=>{$('#fim').hidden=false},ok?700:1000);
}

/* ===== Passagem do tempo ===== */
function passarTempo(){
  aplicar(CFG.tempo);
  const e=$('#hIdade');e.classList.remove('flash');void e.offsetWidth;e.classList.add('flash');
}
let ultimo=performance.now();
setInterval(()=>{
  const agora=performance.now(),dt=agora-ultimo;ultimo=agora;
  if(!rodando||document.hidden||dt>2000)return;   // pausa com aba oculta ou tela aberta
  acum+=dt;
  if(acum>=CFG.segundosPorAno*1000){acum-=CFG.segundosPorAno*1000;passarTempo()}
},250);

/* ===== Ações ===== */
const ACOES={
  alimentar:{cls:'comendo',fala:'Nham nham!',ms:1800},
  brincar:{cls:'brincando',fala:'Dook dook dook!',ms:1750},
  descansar:{cls:'dormindo',fala:'Zzz…',ms:2600}
};
function agir(tipo){
  if(!rodando||ocupado)return;
  const a=ACOES[tipo],f=$('#furao');
  ocupado=true;document.body.classList.add('ocupado');vibra(18);
  f.classList.add(a.cls);falar(a.fala);
  if(PROPS[tipo]){const p=$('#prop');p.innerHTML=PROPS[tipo];p.classList.remove('m');void p.offsetWidth;p.classList.add('m')}
  aplicar(CFG[tipo]);
  setTimeout(()=>{f.classList.remove(a.cls);ocupado=false;document.body.classList.remove('ocupado');if(S&&!S.fim)render()},a.ms);
}

function abrirStatus(){
  if(!S||S.fim)return;
  rodando=false;
  $('#sNome').textContent=S.nome;
  $('#sIdade').textContent=`${S.idade} de ${CFG.anosParaVencer} anos`;
  $('#sFome').textContent=`${S.fome} / 100`;
  $('#sFelicidade').textContent=`${S.felicidade} / 100`;
  $('#sCansaco').textContent=`${S.cansaco} / 100`;
  const av=[];
  if(S.fome>=70)av.push('Está com muita fome.');
  if(S.cansaco>=70)av.push('Está exausto.');
  if(S.felicidade<=30)av.push('Anda triste e quer atenção.');
  $('#sTexto').textContent=av.length?av.join(' '):'Está bem, quieto no banco, entre o musgo e a luz.';
  $('#status').hidden=false;
}
$('#fechar').onclick=()=>{$('#status').hidden=true;if(S&&!S.fim)rodando=true};

$$('.acao').forEach(b=>b.onclick=()=>b.dataset.a==='status'?abrirStatus():agir(b.dataset.a));
$('#furao').onclick=()=>{
  if(!rodando||ocupado)return;
  const f=$('#furao');f.classList.remove('pulinho');void f.offsetWidth;f.classList.add('pulinho');
  falar('Dook!');vibra(10);
};
addEventListener('keydown',e=>{
  if(e.target.tagName==='INPUT')return;
  const k=e.key.toLowerCase();
  if(k==='a')agir('alimentar');else if(k==='b')agir('brincar');else if(k==='d')agir('descansar');
  else if(k==='s')$('#status').hidden?abrirStatus():$('#fechar').click();
});

/* ===== Início e fim ===== */
function entrar(){
  $('#inicio').hidden=true;$('#fim').hidden=true;
  acum=0;ocupado=false;document.body.classList.remove('ocupado');
  humorAntes=humor();rodando=true;render();falar('Dook dook!');
}
function telaInicio(){
  const sv=carregar();
  $('#continuar').hidden=!sv;
  $('#inNome').value=sv?sv.nome:'';
  $('#inicio').hidden=false;
}
$('#comecar').onclick=()=>{S={...CFG.inicial,nome:($('#inNome').value.trim()||'Furão').slice(0,14)};entrar()};
$('#continuar').onclick=()=>{S=carregar();entrar()};
$('#denovo').onclick=()=>{$('#fim').hidden=true;telaInicio()};
$('#inNome').addEventListener('keydown',e=>{if(e.key==='Enter')$('#comecar').click()});

telaInicio();