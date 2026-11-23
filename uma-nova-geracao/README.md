# TALKINFU

Um jogo de bichinho virtual, no estilo Talking Tom, disponível em **duas versões**: uma para navegador (computador e celular) e outra como aplicativo Desktop em Java. Você cuida de um furão que mora num banco no meio de uma caverna musgosa, e o objetivo é fazê-lo chegar aos 50 anos.

Nenhuma das versões usa bibliotecas externas nem depende de imagens ou áudios prontos: o furão, o cenário da caverna, as animações e os efeitos sonoros da versão Java são desenhados e gerados diretamente em código.

## Como jogar

- **Alimentar** diminui a fome.
- **Brincar** aumenta a felicidade, mas cansa e dá um pouco de fome.
- **Descansar** diminui o cansaço.
- **Status** mostra os números exatos e como o furão está se sentindo.
- Tocar ou clicar no furão faz carinho. Ele reage com um pulinho e um *"Dook!"*, mas isso não muda nenhum atributo.

No computador (tanto na versão Web quanto na versão Java) também dá para usar o teclado: `A` alimenta, `B` brinca, `D` descansa e `S` abre ou fecha o status. Na versão Java, a tecla `M` liga ou desliga os efeitos sonoros.

## Regras

O tempo passa sozinho: a cada 8 segundos se passa 1 ano da vida do furão. A cada ano:

| Atributo   | Muda em |
|------------|---------|
| Fome       | +3      |
| Felicidade | -3      |
| Cansaço    | +10     |
| Idade      | +1      |

Efeito de cada ação:

| Ação      | Fome | Felicidade | Cansaço |
|-----------|------|------------|---------|
| Alimentar | -25  | +3         | +2      |
| Brincar   | +5   | +15        | +8      |
| Descansar | +4   | 0          | -35     |

Todos os valores ficam entre 0 e 100. O furão começa com fome 20, felicidade 80, cansaço 0 e idade 0.

**Você perde** se a fome chegar a 100, se o cansaço chegar a 100 ou se a felicidade chegar a 0.  
**Você vence** se o furão chegar aos 50 anos.

O tempo pausa quando você abre o Status (ou troca de aba na versão Web). O progresso é salvo automaticamente e dá para continuar depois.

## Como rodar

### Versão Web (`JavaScript/`)
1. Deixe os quatro arquivos da pasta `JavaScript/` juntos.
2. Abra o `index.html` no navegador. Ou, no VS Code, use a extensão Live Server e clique em "Go Live".

A fonte (Cinzel e Lora) vem do Google Fonts. Sem internet o jogo funciona normalmente, só com uma fonte serifada comum no lugar.

### Versão Desktop (`Java/`)
1. Certifique-se de ter o **JDK 17** (ou superior) instalado.
2. Abra o arquivo `Java/src/Main.java` no IntelliJ IDEA ou VS Code e clique em **Run**, ou rode pelo terminal dentro da pasta `uma-nova-geracao`:

```bash
javac -encoding UTF-8 Java/src/Main.java && java -cp Java/src Main
```

## Arquivos

```text
uma-nova-geracao/
├── JavaScript/
│   ├── index.html   estrutura da página, o furão (SVG) e as telas de início, status e fim
│   ├── style.css    visual, animações do furão e layout para celular e computador
│   ├── script.js    regras do jogo, tempo, ações, salvamento (localStorage) e telas
│   └── cenario.js   gera o cenário da caverna em SVG
└── Java/
    └── src/
        └── Main.java  versão desktop completa (Swing, Graphics2D, save local e sons sintetizados)
```

## Personalizando

**Dificuldade.** Na versão Web, no começo do `script.js`, fica o objeto `CFG` com todos os números do jogo. Na versão Java, os valores ficam na classe `Furao` (`ANOS_PARA_VENCER` e métodos `passarTempo`, `alimentar`, `brincar` e `descansar`).

**Cenário.** No começo do `cenario.js` (Web) ou no `Random(11)` de `desenharCenarioCompleto` (Java), troque o número da seed para mudar a disposição do musgo e das pedras. As cores do musgo estão nas constantes `MF`, `MN`, `MB` e `MD`.

**Furão.** Na versão Web, o desenho está no `index.html` dentro da `div` com id `furao`, com expressões controladas pelo atributo `data-e` (`feliz`, `triste`, `faminto` e `cansado`). Na versão Java, o desenho vetorial fica no método `desenharFuraoDetalhado`.

## Upgrades que eu quero fazer futuramente

- Sons e vozes do furão na versão Web (já iniciados com áudio sintetizado na versão Java)
- Mais ações, como banho e passeio
- Fases da vida (filhote, adulto, idoso) com aparências diferentes
- Roupas e acessórios
- Placar de quem chegou mais longe
- Mais cenários inspirados em jogos que eu amo

## Petritt aviso

Este é um projeto de fã, feito por diversão e sem fins lucrativos. Ele se inspira no clima visual de *Hollow Knight: Silksong*, mas o cenário e o furão são desenhos originais em código. O projeto não tem ligação com a Team Cherry nem com os criadores do Talking Tom.