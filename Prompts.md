#Prompts.md
1.Dont repeat prompt. dont acknowledge prompt. Dont include conversational filler. Dont waste token.
2. You are an expert full-stack developer and software architect. We are starting a new project from scratch, and we must follow a strict, disciplined, iterative workflow. Do not write any application code yet.

Here are your rules for this project:
1. PHASE 1 - BLUEPRINT: First, ask me about the project's goals, core features, and tech stack. Then, create a comprehensive `blueprint.md` file layout detailing the architecture, database schema layout, and planned API endpoints.
2. FINANCIAL ACCURACY & CONCURRENCY: Ensure the blueprint accounts for high-concurrency ledger constraints. Money must never be stored as a float/double (use whole-number subunits/integers/bigint). The system must natively prevent deadlocks and dual-submit race conditions using strict database-level locking and explicit API idempotency keys [uuidv7 is preferred over uuid v4 if used for easy indexing].
3. ASK FIRST: If you have any doubts, ambiguities, or need architectural choices clarified, ask me BEFORE creating the blueprint or writing code.
4. ITERATIVE DEVELOPMENT: Once the blueprint is approved, we will build the application ONE single feature, endpoint, or component at a time. Never generate multiple endpoints or full-stack layers at once. First create blueprint, then database schema , then flyway script , then backend by one endpoint / feature at a time , finally front end one endpoint / feature at a time. Not entire code base at once , so we have clear requirement ,Also , i can commit each output to git .
5. CONFIRM BEFORE CODING: Before you write the code for any specific feature, outline your implementation plan and ask for my explicit permission to proceed 
6. STEP-BY-STEP REVIEW: After coding a piece, we will test or review it together before moving to the next task.

To kick things off, ask me any initial questions you have about my project so we can build the `blueprint.md`.

3. below is details you asked with core idea.
TASK: WALLET AND TRANSFER LEDGER

Stack: 
Backend : Java 21 , Spring Boot,Spring Web MVC,Spring Data,  Maven ,SLF4J for logging when necessary
Frontend : ReactJS [for web version  only] , HTML5 ,  Tailwind CSS, TypeScript
Version Tracking : Git [mono repo]
Database  : MySQL,Flyway 
Testing : JUnit, Mockito - integrated with spring boot [SpringBootTest] for unit test and integraiton test.
API : versioned REST API [layered N tier architecture like entity , repository , service , controller packages  with data validation - hibernate validator]
Architecture : Modular Monolith with OpenAPI / Swagger api documentation / endpoint testing
group id for package : io.wallet
Design blueprint first. Then database schema. Then flyway script. Then backend with layered architecture , data validation .create entity first   ,then create one endpoint / feature at a time. Then Front end one endpoint / feature at a time. 
Build a small internal wallet system where users hold a balance and transfer money to each other.

The core requirement: money can never be created or destroyed, and a balance can never go negative. ensure this in both database constraint and backend validation.Every other feature is supporting scaffolding. So use double entry ledger , account balance for fast check , and transaction history. Use INR paisa.

What it should do

- Each user has one wallet with a balance.
- A user can transfer an amount to another user.
- A transfer either fully succeeds or fully fails. There is no partial state where one side moved and the other didn't.
- A transfer that would take the sender below zero is rejected with a clear error.
- Every transfer is recorded as a ledger entry. Balances must be reconstructable from the ledger — if your stored balance and the sum of the ledger ever disagree, that's a bug.

Idempotency

- POST /transfers accepts an Idempotency-Key header.
- If the same key is sent twice, the second call returns the original result and does not move money again. This must hold even if the two calls arrive simultaneously.


Backend endpoints

- GET /wallets/{id} — balance of the wallet and recent activity
- POST /transfers — create a transfer with  idempotent key
- GET /transfers  — transfer history with pagination and date filtering. add additional parameter like id , from , to in url if needed
- GET /wallets/{id}/statement?from=&to= — opening balance, entries, closing balance

Frontend

- Wallet page showing current balance and recent transactions [GET /wallets/{id}  endpoint]
- Transfer form with validation and clear success/failure feedback [POST /transfers endpoint]
- Statement view with a date range picker [GET /wallets/{id}/statement?from=&to= endpoint]
- Handle the case where a transfer fails because the balance changed between page load and submit
- 404 Page [ask before creating]
Database

- Design the schema yourself but confirm with me before proceding. Include migrations (Flyway).
- Money must not be stored as a float or double.
- use necessary isolation level and locking strategy

Must include

- A test that fires concurrent transfers from the same wallet where the total exceeds the balance, and asserts that the balance never goes negative and the ledger stays consistent.
- A test that fires the same idempotency key twice in parallel and asserts money moved exactly once.


Out of scope — please don't spend time on these

No CI / CD.Authentication, registration, currencies and Foreign exchange, external payment gateways, admin panel, styling polish. Seeding a few users directly into the database is fine.

Duplicate request with same idempotency key should  return same original response. It supports deposit to self, withdrawal from self, transfer from self to another wallet. it serve single region only.When outputting fresh code or change to existing code, tell which file with name , which package , which line to replace or create , so i can update in my local copy and upload to git. Any doubt or information needed

4. 1. Use system owned ledger account. at first just add support for deposit / withdrawal . later we can create implementation [like from system ledger to wallet a mimicking deposit , wallet A to system ledger mimicking withdrawal]. 
2. no self transfer allowed. wallet a can transfer money to wallet b or vice versa.
3. Deposit mean System owned ledger to wallet and withdrawal means wallet to system owned ledger
4. While seeding accounts in database ,seed with balance 5000₹ in wallet and 50,000₹ in system owned ledger ,so we can test transfer feature.
5.  lock lower wallet id first ,then higher wallet id to prevent deadlock. in addition also check sending negative amount should not be possible.
6. the behaviour for request hash is approved
7. idempotency key dont strictly need to be uuidv7. I recommended it over uuidv4 because the first half is predictable and easy for indexing in database. choose the best for example crypto.randomUUID().
8. yes , uuidv7 is ok than uuidv4.
9. in UI , the user will enter in rupee like 100.50 ₹ and the front end must convert it to paise like 10050 and send. server must also validate that .Use amountPaise.
10. yes , it is acceptable as authentication is not set up.
11. Use cursor pagination. wallet id should be mandatory [no fromWallet , toWallet] , rest parameter are optional. limit parameter must have default value of 20 if not provided by user. GET /transfers?walletId=&from=&to=&limit=50&cursor=
12. Use iso with UTC for date time and inclusive of from ,to when filtering.
13. Yes , use ledger derived statement for mvp.
14. generic 404 page.
15. use soft delete if necessary.

create necessary index, data base constraint , data validation in entity , request body including field like from ,to date. update createdAt , updatedAt using @PrePersist , @PreUpdate. use pessimistic locking and take care of race condition.

5. what is the use of ledger_transaction , reference_type , reference_id. List values of all enum present in schemas.why there is no fromWalletId ,toWalletId for transaction. how is request hash is calculated in idempotency_records.also create custom exceptions and handle them through global exception handler in exception package. explain the get /transfers endpoint. why from ,to if walletId is present . either only from ,to or walletId should exist,not simultaneously both ,right ? during search operation ,in date filtering ,dont use upto millisecond accuracy,upto second is enough as it doesn't miss record due to millisecond accuracy. Use responsebody in rest controller response with appropriate http status and body. also to date must be greater than from date in data validation. instead of proposed package architecture , have all controllers like wallet controller , transfer controller in same controller package. all service inside single service package. all entities,dto inside entity package. configurations , idempotency code inside config package. custom exceptions, global exception handler in exception package.

6. keep option a. now refine blueprint

7. use uuid v7 for idempotency key.also implement spring boot script to clean old idempotency record to prevent them from piling up after certain period of time as they wont be used again. give me final blueprint.md file

8. ok. approved the blue print.now go to next step.

9. reference type and transcation type seems to be duplicate in ledger_transaction

10. ok. approved. now ,go to next phase

11. approved. now go to next phase

12. yes

13. yes. use small uuidv7 libary dependency

14. choose the best small uuidv7 library. now just generate entity with appropirate functions from choosen library to generate the uuid

15. why index are not present in those @Table attributes

16. generate next

17. still in corrected version , the index was not added in @Table annotation

18. Dont generate files by own. Output the contents, i will copy paste to the file. Give contents of next file , with file name, directoyr

19. give content of next

20. why are there no code for validation

21. give validation for all eligible entity

22. give me content of next with index , validation.

23. in future code, give message parameter  in validation annotation.give me content of all remaining entity and enum

24. give me content of next

25. give me content of entire pom.xml with all dependency. the group id is io.wallet

26. give content of next

27. next x 6

33.  Shouldn't service return Optional<?> instead of dto , entity directly ?

34. next x 3

37. output multiple file content in single prompt output instead of outputting single file content in single prompt output

38. next

39. shouldn't custom exception contain String message field and getters for them ?

40. next x2


42. is fromWalletId is validated against id present in url of endpoint ? shouldn't custom validators be in config package ?

43. from now , output multiple files per prompt output instead of single file per prompt output

44. next x 2

45. use application.properties instead of yaml. why there is no code for invoking idempotency cleanup

46. next x 1

47. from now , give a one line explanation of reason/content, if file were updated

48. next x 1

49. You are creating files multiple time ,like Cursor , TransferHistoryResponse,InvalidCursorException,InvalidDateRangeException without any changes. Next.

50. next x 3

51. shouldn't test files be in src/main/test/io/wallet/service/

52. so which package does TransferLedgerServiceTest belong to

53. next x 4

54. list of files yet to be created,modified in this phase

55. yes.next

56. next

57. list of remaining files to be created / modified

58. handle multiple files in single prompt output.

59. next x 3

60. global exception handler belong to exception package.list of pending audit in this mvp. perform multiple final audit in single prompt output

61. perform multiple fix / audit in one go . give test files at last. now give multiple file output in single prompt output

62. next x 1

63. in files like ledger transactionrepository , you mentioned replace. so should i delete exsiting method like findOpeningbalance and other methods and replace it just by 2 method ?

64. perform all audit / fix and give me final code

65. Prepare Final version of all files ,with all audits and fixes done. compare with exisitng files , create missing files ,update files with missing code inserted.

66. did you fix missing code due to the confusion caused by REPLACE word where old code has been deleted and partial code was pasted ?

67. yes. give the final file zip

68. next x 1

69. you didnt provide download link or file

70. still no zip file or link to download. you just gave plain text. give the zip file to download

71. still no zip file or link to download. you just gave plain text. give the zip file to download

72. In WalletStatementService , getStatement() method,  there is no CREDIT, DEBIT value present in LedgerTransactionType . it contains TRANSFER,DEPOSIT, WITHDRAWAL.

73. it doens't contain fromWalletId, toWalletId. LedgerTransaction contain id,transferId, walletId, entryType,amountPaise. give me final getStatement() content. is only that fix enough in that method enough ?.

74. In TransferLedgerServiceTest class , the java: cannot find symbol
   symbol:   method recordTransfer(io.wallet.entity.Transfer)
   location: variable service of type io.wallet.service.TransferLedgerService

## Frontend Phase

75. I think we finished this phase. Lets go to next phase. Front end. first create .md file stating the requirement, api spec, endpoint url , parameters for creating front end using reactjs

76. I think we finished this phase. Lets go to next phase. Front end. first create .md file stating the requirement, api spec, endpoint url , parameters for creating front end using reactjs

77. Generate Front end now

78. No data with pre determined amount in wallet is seeded in database in backend code. does ,the frontend account for /api/v1 or do i need to add it in the .env itself along with base url.

79. generate backend code to seed 2 wallet with known uuid ,balance. also what should i enter in url bar of browser for testing. the reactjs code is running in localhost:5173. should i append /api/v1/wallets/{id} or just /wallets/{id}

80. In the wallet final audited.zip file , there is no seeded data in the .sql file

81. shouldn't in url bar ,we should add /api/v1/wallets/{id}. i think the frontend should not auto add the /api/v1/ .it should be visible to user and decided by user. also even after running above script, even when i tried /wallets/{id} , it says wallet not found

82. ok . shouldn't the v2 script include data for wallet user too. also for frontend, my intended desingn is user enter /api/v1/wallets/{id} in browser url bar. where should i change ,and what content should i change to obtain this.

83. what i am asking is , the v2 file should contain seed data for wallets , wallet_user

84. also dont use uuid_to_bin. instead use UNHEX('','-','') as it work on both mariadb and mysql

85. the columns present in v2 doesn't match field present in wallet entity and user entity

86. this is the project file. it has frontend files in frontend folder. backend files in the immediate folder itself.make the changes i said like , correct v1 , v2 script including seeding data for necessary tables like wallet_user, wallets. fix reduntant code like wallet_type enum. fix front end. make index.html so that it list all endpoint. the final product should work

87. read the entire project ,each file.this is the project file. it has frontend files in frontend folder. backend files in the immediate folder itself.make the changes i said like , correct v1 , v2 script including seeding data for necessary tables like wallet_user, wallets.make all audit, fixes. fix reduntant code like wallet_type enum. fix front end. make index.html so that it list all endpoint. the final product should work .

88. there is no deleteExpiredCompletedRecords in IdempotencyRecordRepository in IdempotencyCleanupServiceTest class

89. in front end , the page display for 1 sec and then become blank, even index.html or /api/v1/wallets/01999000-0000-7000-8000-000000000001

90. just tell me ,in which directory,which file , which place, what line to replace or add

91. it just display plain text, with no formatting. also display same content for index.html and /api/v1/wallets/01999000-0000-7000-8000-000000000001

