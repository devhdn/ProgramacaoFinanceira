# 🛡️ Extensão Sankhya: Validação de Período Financeiro (TGFFIN)

Este projeto contém um **Evento Programável Java** (Action Event) desenvolvido para o ERP Sankhya. A extensão intercepta operações na tabela `TGFFIN` (Financeiro) para validar se o vencimento de um título respeita as janelas de planejamento definidas pela controladoria.

---

## 🎯 Objetivo

Garantir a integridade do planejamento financeiro, impedindo a inserção ou alteração de títulos com Data de Vencimento (`DTVENC`) em períodos que já foram encerrados ou que ainda não foram abertos para programação.

---

## 📂 Estrutura do Projeto

O ambiente está configurado para desenvolvimento no **VS Code**, seguindo as melhores práticas de organização:

* **`src/`**: Código-fonte Java (`.java`).
* **`lib/`**: Dependências do ERP (JARs do Sankhya como `san-mge.jar`, `jape.jar`). 
    * *Nota: Estes arquivos estão ignorados no Git para evitar erros de limite de tamanho (100MB).*
* **`bin/`**: Artefatos compilados (`.class`).
* **`.gitignore`**: Configurado para proteger o repositório de arquivos binários pesados.

---

## ⚙️ Configuração do Banco de Dados

A validação consome dados de uma tabela personalizada de períodos. Certifica-te de que a estrutura abaixo existe no teu ambiente:

| Campo | Tipo | Descrição |
| :--- | :--- | :--- |
| `DTINICIO` | DATE | Data inicial do período permitido. |
| `DTFIM` | DATE | Data final do período permitido. |
| `STATUS` | CHAR(1) | 'A' para Aberto, 'F' para Fechado. |

> **Importante:** O método `isPeriodoFechado` na classe `ProgramacaoFinanceira.java` deve ser revisado para garantir que o SQL interno coincida com o nome da tua tabela real (ex: `AD_PERIODOFIN`).

---

## 🚀 Instalação e Deploy

1.  **Dependências:** Copia as bibliotecas do servidor Sankhya (`/home/mgeweb/lib`) para a pasta `lib/` local.
2.  **Compilação:** Compila o código para gerar o arquivo `.class`.
3.  **Deploy:**
    * Faz o upload do `.class` ou do pacote `.jar` para o servidor (diretório de extensões).
    * No Sankhya, acede a **Dicionário de Dados** > Tabela `TGFFIN` > Aba **Eventos**.
    * Regista a classe: `br.com.s3tech.ProgramacaoFinanceira`.
    * Seleciona o momento: **Antes de Inserir** e **Antes de Alterar**.

---

## 🚦 Regras de Negócio (Fluxo de Lógica)



1.  **Interceptação:** O evento é disparado antes de qualquer gravação na `TGFFIN`.
2.  **Coleta de Dados:** O sistema lê a `DTVENC` do registro que está a ser manipulado.
3.  **Consulta de Período:** Executa uma query para verificar se existe um período "Aberto" que compreenda a data em questão.
4.  **Ação:**
    * **Sucesso:** O processo segue normalmente.
    * **Bloqueio:** Uma `PersistenceException` é lançada, interrompendo a transação (**Rollback**) e exibindo um alerta pop-up ao utilizador.

---

## 🛠️ Solução de Problemas (FAQ)

### O Push para o GitHub falhou (Erro de limite de 100MB)?
Se tentares subir arquivos `.jar` grandes, o GitHub bloqueará o envio. Mesmo que apagues o arquivo na pasta, ele permanece no histórico do Git.
* **Solução:** Utiliza o comando `git reset --hard origin/main` para limpar os commits locais problemáticos e garante que a linha `*.jar` esteja no teu `.gitignore`.

---
*Desenvolvido por S3 Tech - 2026*