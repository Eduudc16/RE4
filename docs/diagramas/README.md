# Diagramas do Mercador

Diagrama de classes e diagrama Entidade-Relacionamento (MER), gerados a partir das entidades implementadas em `src/mercador/model` e das tabelas criadas em `mercador.database.ConexaoSQLite`.

Os arquivos-fonte (`.mmd`) estão nesta pasta e podem ser abertos/editados em:
- [mermaid.live](https://mermaid.live) — cola o conteúdo do arquivo e exporta PNG/SVG
- [draw.io](https://app.diagrams.net) — Extras → Edit Diagram → cola o conteúdo Mermaid

## Diagrama de classes

Reflete as 7 classes de modelo do projeto e como elas se referenciam entre si em memória (por id, sem navegação de objeto direta, exceto `ItemInventario`).

```mermaid
classDiagram
    class Categoria {
        -int id
        -String nome
    }
    class Item {
        -int id
        -String nome
        -String descricao
        -double preco
        -int categoriaId
        -String categoriaNome
    }
    class Arma {
        -int id
        -int itemId
        -String itemNome
        -int dano
        -int capacidade
        -int velocidadeRecarga
        -int poderTiro
    }
    class Upgrade {
        -int id
        -int armaId
        -String tipo
        -int nivel
        -double custo
        -boolean aplicado
    }
    class Cliente {
        -int id
        -String nome
        -double dinheiro
    }
    class Transacao {
        -int id
        -int clienteId
        -Integer itemId
        -String tipo
        -int quantidade
        -double valorTotal
        -String data
    }
    class ItemInventario {
        -Item item
        -int quantidade
        +getItem() Item
        +getQuantidade() int
    }

    Categoria "1" --> "0..*" Item : classifica
    Item "1" --> "0..1" Arma : especializada como
    Arma "1" --> "0..*" Upgrade : recebe
    Cliente "1" --> "0..*" Transacao : realiza
    Item "0..1" --> "0..*" Transacao : referenciada em
    Cliente "1" --> "0..*" ItemInventario : possui
    ItemInventario "*" --> "1" Item : refere-se a
```

- `Item` é a entidade genérica do catálogo; `Arma` é uma especialização opcional — só existe quando o item é uma arma (relação 1 para 0..1).
- `Upgrade` pertence sempre a uma `Arma`, nunca a um item genérico. `aplicado` indica se o upgrade já foi comprado.
- `Transacao` aponta para o `Cliente` que operou e, opcionalmente, para o `Item` envolvido (`itemId` é `Integer`, aceita nulo).
- `ItemInventario` é um objeto de leitura (item + quantidade) montado a partir da tabela `inventario_cliente`.

## Diagrama ER (MER)

Baseado nas 7 tabelas e chaves estrangeiras definidas em `ConexaoSQLite.criarTabelas`.

```mermaid
erDiagram
    CATEGORIA {
        integer id PK
        text nome
    }
    CLIENTE {
        integer id PK
        text nome
        real dinheiro
    }
    ITEM {
        integer id PK
        text nome
        text descricao
        real preco
        integer categoria_id FK
    }
    ARMA {
        integer id PK
        integer item_id FK
        integer dano
        integer capacidade
        integer velocidade_recarga
        integer poder_tiro
    }
    UPGRADE {
        integer id PK
        integer arma_id FK
        text tipo
        integer nivel
        real custo
        integer aplicado
    }
    TRANSACAO {
        integer id PK
        integer cliente_id FK
        integer item_id FK
        text tipo
        integer quantidade
        real valor_total
        text data
    }
    INVENTARIO_CLIENTE {
        integer id PK
        integer cliente_id FK
        integer item_id FK
        integer quantidade
    }

    CATEGORIA ||--o{ ITEM : classifica
    ITEM ||--o| ARMA : "pode ser"
    ARMA ||--o{ UPGRADE : recebe
    CLIENTE ||--o{ TRANSACAO : realiza
    ITEM ||--o{ TRANSACAO : "aparece em"
    CLIENTE ||--o{ INVENTARIO_CLIENTE : possui
    ITEM ||--o{ INVENTARIO_CLIENTE : "esta em"
```

- `item.categoria_id` é **NOT NULL** — todo item precisa de categoria (1 categoria → 0..N itens).
- `arma.item_id` é 1 para 1 opcional: nem todo item tem uma linha em `arma`, mas toda arma referencia exatamente um item.
- `inventario_cliente` guarda o que o cliente possui; tem `UNIQUE (cliente_id, item_id)`, então cada item aparece uma vez por cliente, com a quantidade.
- `upgrade.aplicado` é `INTEGER` (0/1), default 0.
- `upgrade.arma_id` e `transacao.cliente_id` são **NOT NULL**; `transacao.item_id` é a única FK que aceita nulo no schema atual.
