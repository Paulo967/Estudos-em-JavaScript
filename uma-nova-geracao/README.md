# TALKINFU

Um jogo de bichinho virtual, no estilo Talking Tom, que roda direto no navegador, no computador e no celular. Você cuida de um furão que mora num banco no meio de uma caverna musgosa. O objetivo é fazê-lo chegar aos 50 anos.

Feito com HTML, CSS e JavaScript puros. Não precisa instalar nada, não usa bibliotecas e não depende de nenhuma imagem: o furão e o cenário são desenhados em código (SVG).

## Como jogar

- **Alimentar** diminui a fome.
- **Brincar** aumenta a felicidade, mas cansa e dá um pouco de fome.
- **Descansar** diminui o cansaço.
- **Status** mostra os números exatos e como o furão está se sentindo.
- Tocar no furão faz carinho. Ele reage, mas isso não muda nenhum atributo.

No computador também dá para usar o teclado: `A` alimenta, `B` brinca, `D` descansa e `S` abre ou fecha o status.

## Regras

O tempo passa sozinho: a cada 8 segundos se passa 1 ano da vida do furão. A cada ano:

| Atributo  | Muda em |
|-----------|---------|
| Fome      | +3      |
| Felicidade| -3      |
| Cansaço   | +10     |
| Idade     | +1      |

Efeito de cada ação:

| Ação      | Fome | Felicidade | Cansaço |
|-----------|------|------------|---------|
| Alimentar | -25  | +3         | +2      |
| Brincar   | +5   | +15        | +8      |
| Descansar | +4   | 0          | -35     |

Todos os valores ficam entre 0 e 100. O furão começa com fome 20, felicidade 80, cansaço 0 e idade 0.

**Você perde** se a fome chegar a 100, se o cansaço chegar a 100 ou se a felicidade chegar a 0.
**Você vence** se o furão chegar aos 50 anos.

O tempo pausa quando você abre o Status ou troca de aba. O progresso é salvo no próprio aparelho e dá para continuar depois.

## Como rodar

1. Baixe os quatro arquivos e deixe todos na mesma pasta.
2. Abra o `index.html` no navegador. Ou, no VS Code, use a extensão Live Server e clique em "Go Live".

A fonte (Cinzel e Lora) vem do Google Fonts. Sem internet o jogo funciona normalmente, só com uma fonte serifada comum no lugar.

## Arquivos

```
index.html   estrutura da página, o furão (SVG) e as telas de início, status e fim
style.css    visual, animações do furão e layout para celular e computador
script.js    regras do jogo, tempo, ações, salvamento e telas
cenario.js   gera o cenário da caverna em SVG
```

## Personalizando

**Dificuldade.** No começo do `script.js` fica o objeto `CFG`, com todos os números do jogo: segundos por ano, valores iniciais, efeito do tempo e de cada ação e a idade para vencer.

**Cenário.** No começo do `cenario.js`, troque o número de `seed` para mudar a disposição do musgo e das pedras. As cores do musgo estão nas linhas `MF`, `MN`, `MB` e `MD`.

**Furão.** O desenho está no `index.html`, dentro da `div` com id `furao`. As cores do pelo e da máscara são os valores hexadecimais do SVG, e as expressões são controladas pelo atributo `data-e` (`feliz`, `triste`, `faminto` e `cansado`).

## Upgrades que eu quero fazer futuramente

- Sons e vozes do furão
- Mais ações, como banho e passeio
- Fases da vida (filhote, adulto, idoso) com aparências diferentes
- Roupas e acessórios
- Placar de quem chegou mais longe
- mais cenarios insprados em jogos que eu amo

## Petritt aviso

Este é um projeto de fã, feito por diversão e sem fins lucrativos. Ele se inspira no clima visual de *Hollow Knight: Silksong*, mas o cenário e o furão são desenhos originais em código. O projeto não tem ligação com a Team Cherry nem com os criadores do Talking Tom.