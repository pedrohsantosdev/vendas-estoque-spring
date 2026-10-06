# Vendas & Estoque API

API REST para cadastro de clientes e produtos, registro de vendas e controle de movimentações de estoque.

Projeto pessoal de backend com Java e Spring Boot, desenvolvido para aplicar regras de negócio, transações, persistência relacional e testes automatizados. A aplicação e o MySQL podem ser executados juntos com Docker Compose.

## Funcionalidades

- Cadastro, consulta, atualização e exclusão de clientes e produtos.
- Busca de produto por código e listagem de produtos com estoque baixo.
- Cadastro de vendas com um ou mais produtos.
- Cálculo do subtotal de cada item e do total da venda.
- Registro de entradas e saídas de estoque.
- Cancelamento de vendas com devolução dos produtos ao estoque.
- Atualização do motivo de uma movimentação.
- Tratamento de erros com respostas HTTP.
- Testes unitários, testes da camada web e testes de integração com MySQL.

## Tecnologias

| Tecnologia | Uso |
|---|---|
| Java 25 | Linguagem |
| Spring Boot 4.1.1 | Configuração e execução da aplicação |
| Spring Web MVC | Endpoints REST |
| Spring Data JPA e Hibernate | Persistência e mapeamento das entidades |
| MySQL | Banco de dados |
| Jakarta Validation | Validação dos DTOs de entrada |
| Maven Wrapper | Build e execução dos testes |
| JUnit e Mockito | Testes e simulação de dependências |
| MockMvc | Testes dos endpoints |
| Testcontainers | MySQL isolado nos testes de integração |
| Docker e Docker Compose | Construção da imagem e execução da API com MySQL |

O Docker Compose utiliza MySQL `8.4.7`. Os testes com Testcontainers utilizam MySQL `8.0.36`.

## Regras de negócio

### Produtos e estoque

- O cadastro de um produto pela API inicia seu estoque em zero.
- Nome e código são obrigatórios.
- O preço deve ser maior que zero.
- O estoque mínimo deve ser maior ou igual a zero.
- O serviço rejeita códigos já utilizados por outro produto.
- Um produto está com estoque baixo quando `quantidadeEstoque <= estoqueMinimo`.
- Entradas aumentam o estoque e saídas diminuem o estoque.
- A quantidade de uma movimentação deve ser maior que zero.
- Uma saída é rejeitada quando sua quantidade supera o estoque consultado do produto.
- A atualização de uma movimentação altera somente o motivo.
- A API não disponibiliza exclusão de movimentações.

### Vendas

- Uma venda deve informar um cliente e uma lista não vazia de itens.
- Cada item informa o produto e uma quantidade positiva.
- Produtos repetidos na mesma requisição são rejeitados.
- O preço unitário do item é obtido do cadastro do produto no momento da venda.
- Venda, itens, alterações de estoque e movimentações são processados na mesma transação.
- Falhas durante esse processamento desfazem as alterações da transação.
- Uma venda é cadastrada com status `CONFIRMADA`.
- O cancelamento gera uma entrada de estoque para cada item e altera o status para `CANCELADA`.
- Cancelar novamente uma venda já cancelada não gera novas devoluções.
- Exclusões impedidas por vínculos entre registros retornam HTTP `409 Conflict`.

## Executar com Docker

Os comandos abaixo são para PowerShell, executados na raiz do repositório.

### Requisitos

- Docker Desktop em execução, com suporte a contêineres Linux e Docker Compose.
- Git, caso utilize a clonagem abaixo.

Para executar a API pelo Docker, o Java e o Maven são fornecidos pelas imagens utilizadas no build. O JDK 25 no computador é necessário para executar os testes pelo Maven Wrapper.

### 1. Obter o projeto

```powershell
git clone https://github.com/pedrohsantosdev/vendas-estoque-spring.git
cd vendas-estoque-spring
```

### 2. Configurar as senhas

Na primeira configuração, copie o arquivo de exemplo. Se já possuir um `.env`, mantenha seus valores:

```powershell
Copy-Item .env.example .env
```

No Linux ou macOS, o comando equivalente é `cp .env.example .env`.

Edite o `.env` e substitua os valores abaixo por duas senhas de sua escolha:

```dotenv
MYSQL_PASSWORD=defina_uma_senha_local
MYSQL_ROOT_PASSWORD=defina_outra_senha_para_root
```

`MYSQL_PASSWORD` é a senha do usuário utilizado pela aplicação. `MYSQL_ROOT_PASSWORD` é a senha do administrador do MySQL e é passada somente ao serviço do banco.

O Compose lê o `.env` e fornece as variáveis aos contêineres. O `.gitignore` exclui esse arquivo do versionamento, e o `.dockerignore` o exclui do contexto de construção da imagem. Mantenha apenas valores de exemplo no `.env.example`.

### 3. Iniciar a API e o MySQL

```powershell
docker compose up -d --build
```

O comando constrói a imagem da aplicação e inicia os serviços `db` e `backend`. Na primeira execução, o download das imagens e das dependências pode levar alguns minutos.

O MySQL possui uma verificação de saúde, e o Compose aguarda o banco ficar disponível antes de iniciar o backend. O Spring Boot ainda precisa concluir sua inicialização antes de receber requisições.

Confira o estado dos serviços:

```powershell
docker compose ps
```

O serviço `db` deve aparecer como `healthy`, e o `backend` deve permanecer em execução.

### 4. Testar a API

Endereço base: **[http://localhost:8081](http://localhost:8081)**.

No Postman, envie:

```http
GET http://localhost:8081/produtos
```

Ou consulte pelo PowerShell:

```powershell
Invoke-RestMethod -Uri http://localhost:8081/produtos
```

A resposta esperada é `200 OK`, com uma lista de produtos. A rota `/` não possui uma página inicial; utilize os endpoints documentados neste README.

### Conexão entre os serviços

| Configuração | Valor no Docker Compose |
|---|---|
| Serviço da aplicação | `backend` |
| API no computador | `http://localhost:8081` |
| Porta da aplicação dentro do contêiner | `8080` |
| Serviço e host do banco na rede do Compose | `db` |
| Porta interna do MySQL | `3306` |
| Banco | `controle_vendas_estoque` |
| Usuário da aplicação | `dev_pedro` |
| Senha da aplicação | Valor de `MYSQL_PASSWORD` no `.env` |

O Compose fornece `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` e `SPRING_DATASOURCE_PASSWORD` ao backend. Essas variáveis substituem a configuração de conexão padrão do `application.properties`.

O MySQL fica acessível pela rede interna do Compose; sua porta não é publicada no computador. A API é publicada apenas no endereço local `127.0.0.1:8081`.

### Logs, reinicialização e persistência

Para acompanhar os logs da aplicação:

```powershell
docker compose logs -f backend
```

Para consultar as últimas mensagens do banco:

```powershell
docker compose logs --tail 100 db
```

`Ctrl+C` encerra o acompanhamento dos logs, mantendo os serviços em execução.

Para parar os serviços:

```powershell
docker compose stop
```

Para iniciá-los novamente, respeitando a dependência entre aplicação e banco:

```powershell
docker compose up -d
```

Após alterar o código Java, reconstrua a imagem com `docker compose up -d --build`.

Os dados do MySQL são armazenados no volume `db-data`. Eles são preservados ao parar ou recriar os contêineres. As senhas e o usuário informados ao MySQL são definidos na primeira inicialização do volume; editar o `.env` depois disso não altera automaticamente as credenciais já gravadas no banco.

### Como a imagem é construída

O Dockerfile utiliza duas etapas:

1. `maven:3.10.0-eclipse-temurin-25-alpine` compila o projeto e gera o JAR.
2. `eclipse-temurin:25-jre-alpine` recebe o JAR e executa a aplicação.

A pasta `target/` é gerada dentro da etapa de construção. Não é necessário gerá-la previamente no computador.

O build utiliza `-DskipTests`: os testes são compilados, mas não executados durante a criação da imagem. Os testes com Testcontainers devem ser executados separadamente, em um ambiente com acesso ao Docker, conforme a seção [Testes](#testes).

### Dados iniciais

Fora do perfil `test`, a classe `TestConfig` cria clientes, produtos, vendas e itens demonstrativos quando as quatro tabelas correspondentes estão vazias.

Os registros demonstrativos são inseridos diretamente pelos repositórios. Para experimentar o fluxo completo de venda e movimentação de estoque, utilize os endpoints abaixo.

## Endpoints

### Clientes

| Método | Rota | Operação |
|---|---|---|
| GET | `/clientes` | Listar clientes |
| GET | `/clientes/{id}` | Buscar cliente |
| POST | `/clientes` | Cadastrar cliente |
| PUT | `/clientes/{id}` | Atualizar cliente |
| DELETE | `/clientes/{id}` | Excluir cliente |

### Produtos

| Método | Rota | Operação |
|---|---|---|
| GET | `/produtos` | Listar produtos |
| GET | `/produtos/{id}` | Buscar produto |
| GET | `/produtos/codigo?numerobarra=PRD-001` | Buscar produto por código |
| GET | `/produtos/baixoestoque` | Listar produtos com estoque baixo |
| POST | `/produtos` | Cadastrar produto |
| PUT | `/produtos/{id}` | Atualizar produto |
| DELETE | `/produtos/{id}` | Excluir produto |

### Vendas

| Método | Rota | Operação |
|---|---|---|
| GET | `/vendas` | Listar vendas |
| GET | `/vendas/{id}` | Buscar venda |
| POST | `/vendas` | Cadastrar venda com itens |
| PATCH | `/vendas/{id}` | Cancelar venda, sem corpo na requisição |
| DELETE | `/vendas/{id}` | Excluir venda, sujeito aos vínculos existentes |

O cancelamento devolve o estoque. A exclusão é uma operação distinta e pode ser impedida pelos itens e movimentações vinculados à venda.

### Movimentações

| Método | Rota | Operação |
|---|---|---|
| GET | `/movimentacoes` | Listar movimentações |
| GET | `/movimentacoes/{id}` | Buscar movimentação |
| POST | `/movimentacoes` | Registrar entrada ou saída manual |
| PATCH | `/movimentacoes/{id}` | Atualizar somente o motivo |

## Exemplo de uso

Nos exemplos, utilize `Content-Type: application/json` quando houver corpo. Substitua os IDs ilustrativos pelos valores retornados pela sua API; a carga inicial pode já ter utilizado alguns IDs.

### 1. Cadastrar um cliente

`POST /clientes`

```json
{
  "nome": "Ana Souza",
  "email": "ana.souza@example.com",
  "telefone": "11988887777"
}
```

Resposta esperada: `201 Created`, com o cliente cadastrado e seu endereço no cabeçalho `Location`.

### 2. Cadastrar um produto

`POST /produtos`

```json
{
  "nome": "Mouse sem fio",
  "codigo": "DEMO-001",
  "preco": 79.90,
  "estoqueMinimo": 3
}
```

O produto começa com estoque zero. Use um código ainda não cadastrado e guarde o ID retornado.

### 3. Repor o estoque

`POST /movimentacoes`

```json
{
  "produtoId": 1,
  "tipoMovimentacao": "ENTRADA",
  "quantidade": 10,
  "motivo": "Reposição inicial"
}
```

Para o produto recém-cadastrado, o estoque passa de zero para dez. A movimentação manual não possui uma venda vinculada.

### 4. Cadastrar uma venda

`POST /vendas`

```json
{
  "clienteId": 1,
  "itens": [
    {
      "produtoId": 1,
      "quantidade": 2
    }
  ]
}
```

O array `itens` aceita outros produtos, cada um com seu ID e quantidade. O mesmo produto não pode aparecer duas vezes.

Neste exemplo, o estoque passa de dez para oito e o valor total da venda é `159.80`. A resposta contém o ID, o momento, o status, o resumo do cliente, os itens e o valor total.

### 5. Cancelar a venda

`PATCH /vendas/{id}`

Envie a requisição sem corpo, utilizando o ID da venda criada. O estoque volta para dez e o status passa a `CANCELADA`.

### 6. Corrigir o motivo de uma movimentação

`PATCH /movimentacoes/{id}`

```json
{
  "motivo": "Reposição recebida e conferida"
}
```

## Respostas e erros

| Status | Situação |
|---|---|
| 200 | Consulta, atualização ou cancelamento concluído |
| 201 | Cadastro concluído, com cabeçalho `Location` |
| 204 | Exclusão concluída, sem corpo na resposta |
| 400 | Entrada inválida ou produtos repetidos na venda |
| 404 | Recurso não encontrado |
| 409 | Estoque insuficiente, código de produto em uso ou violação de integridade dos dados |

As exceções tratadas pelo `ResourceExceptionHandler` utilizam a estrutura abaixo. Exemplo de cliente não encontrado:

```json
{
  "momento": "2026-10-01T12:00:00Z",
  "status": 404,
  "error": "Recurso não encontrado",
  "mensagem": "Recurso não encontrado 99",
  "caminho": "/clientes/99"
}
```

## Testes

Com o **JDK 25 configurado** e o **Docker Desktop em execução**, rode a suíte na raiz do projeto. O Maven Wrapper está incluído no repositório:

```powershell
.\mvnw.cmd test
```

Para executar somente uma classe:

```powershell
.\mvnw.cmd "-Dtest=ProdutoRepositoryIntegrationTest" test
```

No Linux ou macOS, utilize `./mvnw test` (conceda permissão de execução ao script com `chmod +x mvnw`, se necessário).

Os testes de integração criam seus próprios contêineres MySQL. Não é necessário iniciar o banco do Compose para executar esses testes.

| Tipo | Ferramentas | Exemplos de cenários |
|---|---|---|
| Unitário | JUnit e Mockito | Cálculo de valores, regras dos serviços, cadastro e atualização |
| Camada web | MockMvc e serviços simulados | Rotas, validações, DTOs de resposta e tratamento de exceções |
| Integração | Spring Boot, Testcontainers e MySQL | Persistência, consultas, transações e bloqueio de exclusões |

Entre os cenários de integração implementados estão:

- Venda com múltiplos produtos e atualização do estoque.
- Desfazer toda a venda quando um dos produtos possui estoque insuficiente.
- Devolver estoque no cancelamento e impedir uma segunda devolução.
- Consultar produtos com estoque abaixo ou igual ao mínimo.
- Bloquear exclusões de registros com vínculos.
- Inicializar o contexto da aplicação com um banco isolado.

O perfil `test` desativa a carga demonstrativa de `TestConfig`. Os testes configuram `ddl-auto=create` para os bancos dos containers; a configuração local de desenvolvimento utiliza `ddl-auto=update`.

Os relatórios da execução pelo Maven ficam em `target/surefire-reports`.

## Organização do código

| Pacote | Responsabilidade |
|---|---|
| `resources` | Receber requisições HTTP e produzir respostas |
| `dtos` | Representar os dados de entrada e saída |
| `services` | Aplicar regras de negócio e coordenar operações |
| `repositories` | Acessar os dados persistidos |
| `entities` | Mapear entidades e relacionamentos |
| `config` | Configurações e carga demonstrativa |
| `services.exceptions` | Exceções utilizadas pelos serviços |
| `resources.exceptions` | Tratamento HTTP das exceções |

## Autor

Desenvolvido por [Pedro Henrique](https://github.com/pedrohsantosdev) como projeto pessoal de estudos em desenvolvimento backend com Java.

[LinkedIn](https://www.linkedin.com/in/pedrohenriquedev1/)

[Repositório no GitHub](https://github.com/pedrohsantosdev/vendas-estoque-spring)
