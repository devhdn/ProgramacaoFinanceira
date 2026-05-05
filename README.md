# 🛡️ Extensão Sankhya: Validação de Período Financeiro (TGFFIN)

Este projeto contém um **Evento Programável Java** desenvolvido para o ERP Sankhya. A extensão intercepta a inserção de novos registros na tabela `TGFFIN` (Movimentação Financeira) e valida se a Data de Vencimento (`DTVENC`) do título pertence a um período de programação que esteja aberto.

## 🎯 Objetivo

Garantir a integridade do planejamento financeiro, impedindo que usuários (ou processos automáticos) insiram títulos com vencimentos em meses/períodos que já foram encerrados pela controladoria ou diretoria financeira.

---

## 📂 Estrutura do Projeto (VS Code)

O ambiente de desenvolvimento segue o padrão de projetos Java no Visual Studio Code:

* **`src/`**: Contém o código-fonte da extensão (ex: `classes/ProgramacaoFinanceira.java`).
* **`lib/`**: Diretório para armazenar as dependências do Sankhya (arquivos `.jar` como `sankhya-jape.jar`, `sankhya-modelcore.jar`, etc.) necessários para a compilação local.
* **`bin/`**: Pasta de saída onde os arquivos `.class` compilados serão gerados.

> **Nota de Dependência:** Para que o VS Code não acuse erros de sintaxe, certifique-se de copiar as bibliotecas (JARs) do servidor Sankhya (`/home/mgeweb/lib` ou similar) para a pasta `lib` deste projeto.

---

## ⚙️ Pré-requisitos e Configuração (Banco de Dados)

Para que a regra de negócio funcione, o sistema espera que exista uma tabela no banco de dados para controlar os períodos. 

* **Tabela de Períodos:** A query no método `isPeriodoFechado` precisa ser ajustada para apontar para a sua tabela real (ex: `AD_PERIODOFIN`).
* **Campos Necessários:** A tabela deve possuir campos que representem a data de início, data final e o status (ex: Aberto/Fechado) do período.

---

## 🚀 Como Instalar no Sankhya

1. **Compilação:** Compile o projeto utilizando sua IDE (VS Code, Eclipse, etc.) para gerar o arquivo `.class` ou empacote em um `.jar`.
2. **Upload:** Coloque o arquivo compilado na pasta de extensões do servidor Sankhya.
3. **Configuração no Sistema:**
   * Acesse a tela **Dicionário de Dados** no Sankhya.
   * Localize a tabela **`TGFFIN`** (Financeiro).
   * Vá até a aba **Eventos**.
   * Adicione um novo registro e informe o caminho completo da classe: `classes.ProgramacaoFinanceira`.
   * Marque a opção para o evento ser ativado.

---

## 🚦 Comportamento do Sistema

* **Caminho Feliz:** Se a data de vencimento (`DTVENC`) cair em um período **Aberto**, o registro será salvo normalmente na `TGFFIN`.
* **Bloqueio:** Se a data cair em um período **Fechado** (ou inexistente, dependendo da configuração), o evento dispara um `Rollback` no banco e exibe um alerta pop-up na tela do usuário informando que a ação foi bloqueada.