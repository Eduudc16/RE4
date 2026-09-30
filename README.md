# O Mercador

Sistema desktop em Java Swing inspirado no Mercador de Resident Evil 4. Permite comprar, vender e aprimorar itens, além de cadastrar novos itens no catálogo.

## Tecnologias
- Java 21 + Swing
- SQLite (via `org.xerial:sqlite-jdbc`, jar em `lib/`)
- Padrão MVC

## Estrutura do projeto
```
src/mercador/
├── Main.java              # ponto de entrada
├── model/                 # entidades (Cliente, Categoria, Item, Arma, Upgrade, Transacao)
├── database/               # conexão SQLite + DAOs de leitura
├── view/                  # telas Swing
└── controller/            # lógica das telas (CRUD a implementar)
```

## Como rodar (VS Code)
1. Instale a extensão **Extension Pack for Java** (Red Hat).
2. Abra a pasta do projeto no VS Code — o `.vscode/settings.json` já aponta `src` como source root e `lib/*.jar` como biblioteca referenciada.
3. Abra `src/mercador/Main.java` e clique em **Run**.

O banco `database/mercador.db` é criado automaticamente na primeira execução, já com as tabelas e alguns itens pré-cadastrados (armas, munições, curas, tesouros e coletes do jogo).

## Banco de dados (6 tabelas)
`categoria`, `cliente`, `item`, `arma`, `upgrade`, `transacao` — definidas em `mercador.database.ConexaoSQLite`.

## O que já está pronto
- Estrutura MVC completa
- Conexão e inicialização do banco (com dados de exemplo)
- As 4 telas (Comprar, Vender, Aprimorar, Adicionar) + Menu principal, exibindo dados reais do banco

## O que falta implementar (CRUD)
Cada tela já chama um método do respectivo Controller, marcado com `// TODO`. É só implementar a lógica dentro desses métodos:

| Tela | Controller | Método |
|---|---|---|
| Comprar Itens | `ComprarItensController` | `comprarItem` |
| Vender Itens | `VenderItensController` | `venderItem` |
| Aprimorar Itens | `AprimorarItensController` | `aplicarUpgrade` |
| Adicionar Itens | `AdicionarItensController` | `adicionarItem` (Create/Update/Delete) |
