# Personalizações Sankhya - S3Tech

## Visão Geral

Este repositório contém as personalizações e regras de negócio desenvolvidas em Java para integração e controle dentro do ERP **Sankhya Om**. As classes aqui presentes implementam validações específicas de processos comerciais e financeiros da empresa.

---

## Classes Implementadas

### 1. `ValidacaoPagamentoAvista.java`
* **Tipo**: Rotina Java (`RegraNegocioJava`)
* **Tabela Alvo**: `TGFCAB` (Cabeçalho da Nota)
* **Descrição**: Valida as operações de venda. Caso a condição de pagamento informada seja à vista (`CODTIPVENDA = 11`), a rotina bloqueia a continuidade da operação e gera automaticamente uma **Solicitação de Liberação** de limites (Evento `1002`) para a gerência usando a classe nativa `LiberacaoAlcadaHelper`. 

### 2. `ProgramacaoFinanceira.java`
* **Tipo**: Evento Programável Java (`EventoProgramavelJava` - `beforeInsert`)
* **Tabela Alvo**: `TGFFIN` (Financeiro)
* **Descrição**: Atua no momento da inserção de um registro financeiro (Origens `E` ou `F`). A classe verifica se a data de vencimento (`DTVENC`) cai em um período financeiro fechado (tabela `AD_PROFIN`).
* **Exceções da Regra**: A trava é ignorada se o Tipo de Título for `11`, se a TOP da nota estiver em uma lista de exceções (Ex: 900 a 1013), ou se a condição de negociação for do subtipo 1 (À Vista).

---

## Como Instalar no Sankhya (Deploy)

De acordo com a documentação oficial da Dti, a inserção das rotinas no Sankhya deve seguir os passos abaixo:

1. **Geração do Arquivo**: Compile o projeto e gere o arquivo `.jar`. Recomendamos o uso de um ofuscador (como Allatori) para proteger o código-fonte antes do deploy.
2. **Upload do Módulo Java**:
   * Navegue no Sankhya Om até: `Configurações > Avançado > Módulos Java` (ou Bibliotecas Java).
   * Faça o upload do arquivo `.jar` gerado.
3. **Vínculo da Rotina (Dicionário de Dados)**:
   * Navegue até: `Configurações > Dicionário de Dados`.
   * Para a **Regra de Negócio** (`ValidacaoPagamentoAvista`), acesse a aba *Ações - Rotina Java* na tabela `TGFCAB` e cadastre a ação chamando a classe.
   * Para o **Evento Programável** (`ProgramacaoFinanceira`), acesse a aba *Eventos* na tabela financeira correspondente e vincule a classe ao evento de inclusão.

---

## Controle de Versão e Segurança

Para garantir que arquivos compilados pesados ou ofuscados não poluam o repositório, o arquivo `.gitignore` deste projeto está configurado para **bloquear o envio de qualquer arquivo `.jar`**. 