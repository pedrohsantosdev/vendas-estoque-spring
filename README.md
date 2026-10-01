# Vendas & Estoque API

API REST para cadastro de clientes e produtos, registro de vendas e controle de movimentações de estoque.

Projeto pessoal desenvolvido para praticar Java e Spring Boot, com foco em regras de negócio, persistência relacional, validação de requisições e testes automatizados.

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
| Docker Compose | MySQL para desenvolvimento local |

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

## Como executar

Os comandos abaixo são para PowerShell, executados na raiz do repositório.

### Requisitos

- JDK 25 configurado no ambiente.
- Docker Desktop em execução, com suporte a containers Linux.
- Git, caso utilize a clonagem abaixo.

O projeto inclui o Maven Wrapper.

### 1. Obter o projeto

```powershell
git clone https://github.com/pedrohsantosdev/vendas-estoque-spring.git
cd vendas-estoque-spring
```

### 2. Configurar o ambiente

Na primeira configuração, copie o arquivo de exemplo:

```powershell
Copy-Item .env.example .env
```

Edite o arquivo `.env` e defina os valores:

```properties
MYSQL_PASSWORD=defina_uma_senha_local
MYSQL_ROOT_PASSWORD=defina_outra_senha_para_root
```

O arquivo `.env` está incluído no `.gitignore`. O projeto importa suas propriedades por meio de `spring.config.import`.

A configuração de desenvolvimento utiliza:

| Configuração | Valor |
|---|---|
| Host | `localhost`, ou o valor de `MYSQL_HOST` |
| Porta do MySQL | `3306` |
| Banco | `mydatabase` |
| Usuário da aplicação | `myuser` |
| Senha da aplicação | Valor de `MYSQL_PASSWORD` |

### 3. Iniciar o MySQL

```powershell
docker compose up -d
docker compose logs -f mysql
```

Aguarde o MySQL informar que está pronto para receber conexões. Use `Ctrl+C` para sair da visualização dos logs; o container continuará em execução.

O Compose publica a porta `3306`. Se já existir outro MySQL nessa porta, resolva o conflito antes de iniciar o container.

### 4. Iniciar a aplicação

```powershell
.\mvnw.cmd spring-boot:run
```

A API fica disponível em `http://localhost:8080`. Uma consulta inicial pode ser feita em [GET /produtos](http://localhost:8080/produtos).

Para interromper a aplicação, use `Ctrl+C`. Para parar o banco de desenvolvimento:

```powershell
docker compose stop
```

### Dados iniciais

Fora do perfil `test`, a classe `TestConfig` cria clientes, produtos, vendas e itens demonstrativos quando as quatro tabelas correspondentes estão vazias.

A configuração atual também exige que exista o produto de código `PRD-001`. Se o banco já contiver dados, mas esse produto não existir, a inicialização será interrompida com uma mensagem indicando essa necessidade.

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

Com o Docker Desktop em execução, rode a suíte:

```powershell
.\mvnw.cmd test
```

Para executar somente uma classe:

```powershell
.\mvnw.cmd "-Dtest=ProdutoRepositoryIntegrationTest" test
```

Os testes de integração criam seus próprios containers MySQL. Não é necessário iniciar o banco do Compose para executar esses testes.

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

Desenvolvido por [Pedro](https://github.com/pedrohsantosdev) como projeto pessoal de estudos em desenvolvimento backend com Java.

[Repositório no GitHub](https://github.com/pedrohsantosdev/vendas-estoque-spring)
