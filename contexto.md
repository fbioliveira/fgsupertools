# Contexto de Desenvolvimento - Projeto App

## 1. Problemas Resolvidos

### Erro de Integridade do Room (IllegalStateException)
- **Problema:** O aplicativo estava falhando com um `IllegalStateException` informando que o schema do banco de dados mudou, mas a versão não foi incrementada.
- **Solução:** A versão do banco de dados em `AppDatabase.kt` foi incrementada de `3` para `4`. Como o `fallbackToDestructiveMigration()` já estava configurado, o Room recriou o banco com o novo schema.

## 2. Implementações de UI (Notificações)

### Status com Badges nos Cards
- **Alteração:** Adicionado um badge visual nos cards de notificação na aba de "Notificações".
- **Visual:**
    - **"Não lido"**: Texto em verde neon (`NeonGreen`) com fundo semitransparente.
    - **"Lido"**: Texto em cinza (`Color.Gray`) com fundo semitransparente.
- **Arquivo alterado:** `MainActivity.kt` (Componente `NotificationListItem`).

### Marcar como Lido ao Clicar
- **Comportamento:** Quando o usuário clica em um card de notificação para expandi-lo e ver o conteúdo, o status daquela notificação é atualizado para "Lido" no banco de dados.
- **Lógica:** O evento `clickable` do `Card` agora dispara a função `markAsRead(notification)` do `NotificationViewModel` quando o estado `expanded` torna-se verdadeiro.
- **Arquivos alterados:** `MainActivity.kt`, `NotificationViewModel.kt`.

## 3. Lógica de Negócio e Filtros

### Contador de Notificações no Dashboard (Home)
- **Regra:** O contador de notificações exibido no dashboard da tela inicial deve mostrar apenas o número de notificações **não lidas** que são efetivamente exibidas na lista.
- **Refinamento do DAO:** A query `getUnreadNotificationCount()` no `NotificationDao.kt` foi atualizada para filtrar notificações com `isRead = 0` E que possuam um título válido (`title IS NOT NULL AND title != ''`), mantendo consistência com o filtro da UI.
- **Arquivos alterados:** `MainActivity.kt` (Uso de `unreadCount`), `NotificationDao.kt` (Query SQL).

## 4. Estrutura do Projeto (Arquivos Principais)
- `AppDatabase.kt`: Configuração do banco de dados Room.
- `NotificationEntity.kt`: Modelo de dados da notificação.
- `NotificationDao.kt`: Interface de acesso aos dados (SQL).
- `NotificationViewModel.kt`: Gerenciamento de estado da UI de notificações.
- `MainActivity.kt`: Implementação das telas e componentes Compose (Home, Recorder, Notifications).
