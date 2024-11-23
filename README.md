
# AWS URL Shortener

Este projeto é um encurtador de URL baseado em AWS, que utiliza duas funções Lambda e o S3 para armazenamento. Ele permite criar URLs encurtadas que redirecionam para os URLs originais.

## Visão Geral

### Funcionamento

1. **Criação de URL Encurtada:**
   - Recebe uma URL fornecida pelo usuário via API Gateway.
   - Gera um código único (UUID de 8 dígitos).
   - Salva a URL original e o tempo de expiração em um arquivo JSON no S3.
   - Retorna uma URL encurtada padronizada.

2. **Redirecionamento de URL:**
   - Recebe o código curto da URL via API Gateway.
   - Recupera os dados do S3.
   - Redireciona para a URL original se a URL não estiver expirada.

---

## Estrutura do Projeto

- **Lambda 1: Criar URL Encurtada**
  - Recebe um JSON com a URL original.
  - Gera um código UUID único de 8 dígitos.
  - Salva o seguinte conteúdo no S3:
    - Nome do arquivo: `<uuid>.json` (exemplo: `b616f106.json`).
    - Conteúdo do arquivo:
      ```json
      {
        "originalUrl": "https://exemplo.com",
        "expirationTime": 1732390697
      }
      ```
  - Retorna a URL encurtada no formato: `https://<base-url>/<uuid>`.

- **Lambda 2: Redirecionar URL**
  - Recebe o código curto via URL.
  - Busca os dados correspondentes no S3.
  - Verifica se a URL expirou:
    - Se sim, retorna status **410 (Gone)**.
    - Caso contrário, redireciona para a URL original com status **302 (Found)**.

- **API Gateway**
  - Padroniza as URLs de acesso para as funções Lambda:
    - **POST** `/create` → Chama a Lambda de criação.
    - **GET** `/{uuid}` → Chama a Lambda de redirecionamento.

---

## Configuração

### Variáveis de Ambiente

Configure as seguintes variáveis de ambiente para ambas as Lambdas:

- `BUCKET_NAME`: Nome do bucket S3 para armazenar os dados das URLs.
- `BASE_URL`: Base da URL encurtada (exemplo: `https://meusite.com`).
- **Lambda de Criação:**
  - `EXPIRATION_TIME_SECONDS`: Tempo de expiração em segundos (exemplo: `604800` para 7 dias).

### Deploy

1. Faça o deploy das funções Lambda no AWS.
2. Configure o API Gateway para roteamento:
   - `/create` → Lambda de criação.
   - `/redirect/{uuid}` → Lambda de redirecionamento.
3. Crie um bucket S3 para armazenar os dados das URLs.

---

## Exemplo de Uso

### Criar URL Encurtada

**Requisição:**
```http
POST /create HTTP/1.1
Content-Type: application/json

{
  "originalUrl": "https://www.cnnbrasil.com.br"
}
```

**Resposta:**
```json
{
  "shortened-url": "https://meusite.com/b616f106"
}
```

### Redirecionar para URL Original

**Requisição:**
```http
GET /redirect/b616f106 HTTP/1.1
```

**Resposta (302 Found):**
Redireciona para: `https://www.cnnbrasil.com.br`.

### URL Expirada

**Requisição:**
```http
GET /redirect/b616f106 HTTP/1.1
```

**Resposta (410 Gone):**
```json
{
  "message": "This URL has expired."
}
```

---

## Tecnologias Utilizadas

- **AWS Lambda**: Para processamento serverless.
- **AWS S3**: Para armazenamento dos dados das URLs.
- **API Gateway**: Para expor as Lambdas como APIs HTTP.
- **Java**: Linguagem de implementação.
- **Jackson**: Para manipulação de JSON.
- **AWS SDK**: Para interagir com os serviços da AWS.

---

## Melhorias Futuras

- Implementar autenticação nas APIs.
- Adicionar logs centralizados com AWS CloudWatch.
- Criar uma interface frontend para facilitar o uso.

---

## Contribuição

Contribuições são bem-vindas! Sinta-se à vontade para abrir issues ou pull requests neste repositório.

---

## Licença

Este projeto está licenciado sob a [MIT License](LICENSE).
