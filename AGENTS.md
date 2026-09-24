# AGENTS.md — Dragon Hunt

## 1. Informações do projeto

Dragon Hunt é um jogo desktop em Java construído sobre a base do MVP de labirinto com A*. O projeto utiliza Java 21, JavaFX 21.0.8, Maven, JUnit 5 e um ambiente reproduzível com Nix por meio do `flake.nix`.

A aplicação deve separar claramente domínio, aplicação e apresentação. `Main` inicializa o JavaFX; a UI cuida de cena, entrada e animações; o domínio concentra estado e regras do jogo. Classes como `Cell`, `MazeGrid` e `AStarPathfinder` não devem depender de JavaFX e precisam permanecer testáveis sem iniciar a interface gráfica.

A arquitetura deve evoluir de forma incremental, priorizando baixo acoplamento, alta coesão e dependências apontando para o núcleo do domínio. Frameworks e detalhes de infraestrutura não devem determinar as regras do jogo.

## 2. Como contribuir

Antes de alterar o código, leia as classes e testes relacionados à mudança e identifique a responsabilidade correta. Prefira mudanças pequenas, coesas e compatíveis com a estrutura existente. Uma nova abstração deve existir para resolver um problema real de acoplamento, teste, criação de objetos ou evolução do domínio.

Toda funcionalidade que altera comportamento deve possuir testes no nível adequado. Mudanças de UI também devem ser executadas manualmente quando possível, pois testes unitários não garantem que a cena JavaFX esteja corretamente conectada.

Contribuições devem preservar a arquitetura, manter a suíte de testes verde e evitar alterações não relacionadas. Código temporário, arquivos de diagnóstico local e dependências desnecessárias não devem acompanhar uma mudança funcional.

## 3. O que não fazer

Não faça o domínio depender de JavaFX. Não coloque regras de negócio em `Node`, `Pane`, handlers, animações ou outros componentes visuais. A árvore de cena é uma representação do estado do jogo, não a fonte de verdade.

Não use estado global mutável como mecanismo de comunicação. Dependências devem ser recebidas por construtores ou APIs explícitas. Evite service locator, singletons usados apenas por conveniência e classes utilitárias sem responsabilidade coesa.

Não bloqueie a JavaFX Application Thread com operações demoradas, I/O ou cálculos pesados. Não esconda falhas com `catch` genérico, `null` silencioso ou valores mágicos. Não introduza frameworks ou mecanismos de reflexão sem necessidade concreta.

Não altere caches, diretórios gerados pelo Maven, `/nix/store` ou dependências globais para mascarar problemas do projeto. Correções devem ocorrer no código ou na configuração versionada.

## 4. Padrões de codificação

O código deve seguir princípios de Clean Code e SOLID como critérios práticos. Nomes devem revelar intenção, métodos devem ser pequenos e focados, classes devem possuir responsabilidade única e duplicação deve ser evitada sem criar abstrações artificiais. Comentários devem explicar decisões, restrições ou contexto, e não repetir o código.

Prefira código simples, explícito e previsível. Evite métodos com muitos parâmetros, condicionais profundamente aninhadas, efeitos colaterais inesperados, booleanos que controlam múltiplos comportamentos e abstrações prematuras. Uma função deve possuir um propósito claro.

Use Java 21 de forma idiomática. `record` deve ser usado quando representar adequadamente valores imutáveis. `enum` deve representar estados finitos. Coleções expostas pelo domínio devem ser imutáveis ou protegidas por cópia defensiva quando necessário. Exceções devem representar erros reais e não substituir fluxo normal de controle.

O uso de Design Patterns deve partir de uma necessidade concreta. Factory ou Factory Method pode encapsular criação complexa; Strategy pode representar algoritmos intercambiáveis; Adapter pode isolar APIs externas; State pode representar estados relevantes do jogo; Command pode encapsular ações; Observer ou eventos podem desacoplar notificações. Builder pode ser usado quando a construção realmente possuir complexidade. Não introduza um padrão apenas para seguir um catálogo de padrões.

Use `PascalCase` para tipos, `camelCase` para métodos e variáveis e `UPPER_SNAKE_CASE` para constantes. Mantenha imports explícitos, evite classes gigantes e prefira composição a herança quando não existir uma relação real de substituição.

## 5. Padrão arquitetural

O projeto deve seguir uma arquitetura em camadas simples, inspirada nos princípios de Clean Architecture, sem impor complexidade desnecessária. O domínio contém entidades, valores, regras e algoritmos. A camada de aplicação coordena casos de uso e fluxo do jogo. A apresentação JavaFX adapta o domínio para a interface e transforma eventos do usuário em operações da aplicação.

A direção das dependências deve apontar para dentro. A apresentação pode depender da aplicação e do domínio; a aplicação pode depender do domínio; o domínio não deve depender de JavaFX, Maven, Nix, arquivos ou APIs específicas de infraestrutura.

A UI deve funcionar como adaptador. `MazeView` e futuras telas podem conhecer JavaFX, mas não devem duplicar regras de colisão, movimentação, pontuação, busca de caminho ou estados do jogo. O domínio deve produzir resultados que a UI consiga representar.

Cada classe deve possuir uma responsabilidade clara e cada pacote deve representar uma fronteira compreensível. Evite camadas vazias ou interfaces que apenas repetem uma implementação. Interfaces devem existir quando houver uma fronteira arquitetural, necessidade de substituição, teste ou múltiplas implementações.

## 6. Fluxo de desenvolvimento

O fluxo padrão é entender a mudança, implementar, formatar, testar, compilar e executar. Use `mvn test` para a suíte automatizada, `mvn package` para verificar a compilação completa e `./run.sh` ou o mecanismo definido pelo projeto para validar a aplicação JavaFX. Quando disponível, prefira o ambiente Nix para reproduzir as dependências.

Alterações no domínio devem terminar com testes cobrindo comportamento normal, limites e falhas relevantes. Alterações na UI devem incluir verificação manual da abertura da janela, interação, atualização visual, animações e encerramento.

Antes de concluir uma mudança, confirme que o código está formatado, os testes passam, a aplicação compila e a arquitetura continua íntegra. Verifique especialmente se nenhuma dependência de JavaFX vazou para o domínio, se regras de negócio não foram duplicadas na UI e se uma nova abstração ou Design Pattern realmente reduziu a complexidade.

Uma alteração só deve ser considerada concluída quando estiver correta, legível, testável e coerente com a arquitetura. O objetivo é manter um código simples de compreender, seguro para modificar e preparado para evoluir sem introduzir complexidade desnecessária.

## 7. Commits

Os commits devem seguir o padrão Conventional Commits e ser escritos em inglês. O formato esperado é `type(scope): description`, usando tipos como `feat`, `fix`, `refactor`, `test`, `docs`, `build` e `chore` conforme a natureza da alteração.

Cada commit deve ser atômico e representar uma única unidade funcional ou técnica coerente. Uma funcionalidade grande deve ser dividida em commits menores quando necessário, mas cada commit precisa ser autocontido: deve compilar, preservar os testes e não depender de código que só aparecerá em um commit posterior.

Nunca deixe uma funcionalidade parcialmente quebrada em um commit intermediário esperando que o próximo commit a complete. Prefira uma sequência em que cada commit represente um estado válido do projeto.

Evite misturar correção, refatoração não relacionada e nova funcionalidade no mesmo commit. A mensagem deve descrever claramente o propósito da alteração, por exemplo `feat: add player movement` ou `fix: prevent invalid maze traversal`.

Essas regras devem ser tratadas como parte do contrato de desenvolvimento do Dragon Hunt. Qualquer alteração nova deve manter a qualidade do código, a separação arquitetural, a capacidade de teste e a integridade de cada commit.


 - Não commit o AGENTS.md
