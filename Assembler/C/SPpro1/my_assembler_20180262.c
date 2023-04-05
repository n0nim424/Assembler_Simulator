/*
 * 화일명 : my_assembler_00000000.c
 * 설  명 : 이 프로그램은 SIC/XE 머신을 위한 간단한 Assembler 프로그램의 메인루틴으로,
 * 입력된 파일의 코드 중, 명령어에 해당하는 OPCODE를 찾아 출력한다.
 * 파일 내에서 사용되는 문자열 "00000000"에는 자신의 학번을 기입한다.
 */

 /*
  *
  * 프로그램의 헤더를 정의한다.
  *
  */

#define _CRT_SECURE_NO_WARNINGS

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <fcntl.h>

  // 파일명의 "00000000"은 자신의 학번으로 변경할 것.
#include "my_assembler_20180262.h"

/* ----------------------------------------------------------------------------------
 * 설명 : 사용자로 부터 어셈블리 파일을 받아서 명령어의 OPCODE를 찾아 출력한다.
 * 매계 : 실행 파일, 어셈블리 파일
 * 반환 : 성공 = 0, 실패 = < 0
 * 주의 : 현재 어셈블리 프로그램의 리스트 파일을 생성하는 루틴은 만들지 않았다.
 *		   또한 중간파일을 생성하지 않는다.
 * ----------------------------------------------------------------------------------
 */
int main(int args, char* arg[])
{
    if (init_my_assembler() < 0)
    {
        printf("init_my_assembler: 프로그램 초기화에 실패 했습니다.\n");
        return -1;
    }

    if (assem_pass1() < 0)
    {
        printf("assem_pass1: 패스1 과정에서 실패하였습니다.  \n");
        return -1;
    }
    // make_opcode_output("output_20180262.txt");

    make_symtab_output(NULL); // 화면 출력을 위해 NULL 넣음
    make_literaltab_output(NULL); // 화면 출력을 위해 NULL 넣음
    if (assem_pass2() < 0)
    {
        printf("assem_pass2: 패스2 과정에서 실패하였습니다.  \n");
        return -1;
    }

    make_objectcode_output("output_20180262.txt");

    return 0;
}

/* ----------------------------------------------------------------------------------
 * 설명 : 프로그램 초기화를 위한 자료구조 생성 및 파일을 읽는 함수이다.
 * 매계 : 없음
 * 반환 : 정상종료 = 0 , 에러 발생 = -1
 * 주의 : 각각의 명령어 테이블을 내부에 선언하지 않고 관리를 용이하게 하기
 *		   위해서 파일 단위로 관리하여 프로그램 초기화를 통해 정보를 읽어 올 수 있도록
 *		   구현하였다.
 * ----------------------------------------------------------------------------------
 */
int init_my_assembler(void)
{
    int result;

    if ((result = init_inst_file("inst.txt")) < 0)
        return -1;
    if ((result = init_input_file("input.txt")) < 0)
        return -1;
    return result;
}

/* ----------------------------------------------------------------------------------
 * 설명 : 머신을 위한 기계 코드목록 파일을 읽어 기계어 목록 테이블(inst_table)을
 *        생성하는 함수이다.
 * 매계 : 기계어 목록 파일
 * 반환 : 정상종료 = 0 , 에러 < 0
 * 주의 : 기계어 목록파일 형식은 자유롭게 구현한다. 예시는 다음과 같다.
 *
 *	===============================================================================
 *		   | 이름 | 형식 | 기계어 코드 | 오퍼랜드의 갯수 | NULL|
 *	===============================================================================
 *
 * ----------------------------------------------------------------------------------
 */
int init_inst_file(char* inst_file)
{
    FILE* file;
    int errno;
    int inst_index = 0; //전역변수 초기화, 줄 이동할 때 쓸 변수

    file = fopen(inst_file, "r"); // inst_file을 읽기 형식으로 열기

    if (file == NULL) //파일이 없다면
        errno = -1; //음수 리턴
    else // 파일이 있다면
    {

        while (!feof(file)) // 파일 끝까지 반복
        {
            inst_table[inst_index] = (inst*)malloc(sizeof(struct inst_unit)); // 동적메모리 할당
            fscanf(file, "%s\t%s\t%s\t%d", inst_table[inst_index]->name, inst_table[inst_index]->format, &inst_table[inst_index]->op, &inst_table[inst_index]->opn); //tab을 기준으로 읽어서 inst_table의 각 멤버에 넣기
            inst_index++; //다음줄로 이동
        }
        errno = 0; // 정상종료
        fclose(file); // 파일 닫기
    }
    return errno;
}

/* ----------------------------------------------------------------------------------
 * 설명 : 어셈블리 할 소스코드를 읽어 소스코드 테이블(input_data)를 생성하는 함수이다.
 * 매계 : 어셈블리할 소스파일명
 * 반환 : 정상종료 = 0 , 에러 < 0
 * 주의 : 라인단위로 저장한다.
 *
 * ----------------------------------------------------------------------------------
 */
int init_input_file(char* input_file)
{
    FILE* file;
    int errno;
    char* string[MAX_INST]; // 읽은 소스코드를 저장해 둘 배열

    file = fopen(input_file, "r"); // input_file을 읽기 형식으로 열기

    if (file == NULL) //파일이 없다면
        errno = -1; //음수 리턴
    else // 파일이 있다면
    {
        while (!feof(file)) // 파일 끝까지 반복
        {
            fgets(string, MAX_INST, file); // 소스코드를 라인단위로 읽기
            char* newdata = (char*)malloc(sizeof(char) * (strlen(string) + 1)); //새로운 포인터에 동적메모리 할당
            strcpy(newdata, string); // 배열에 저장된 소스코드를 포인터로 복사
            input_data[line_num] = newdata; //소스코드가 복사된 포인터를 input_data에 넣기
            line_num++; //다음 줄로 이동
        }
        errno = 0; // 정상종료
    }
    fclose(file); // 파일 닫기
    return errno;
}

/* ----------------------------------------------------------------------------------
 * 설명 : 소스 코드를 읽어와 토큰단위로 분석하고 토큰 테이블을 작성하는 함수이다.
 *        패스 1로 부터 호출된다.
 * 매계 : 파싱을 원하는 문자열
 * 반환 : 정상종료 = 0 , 에러 < 0
 * 주의 : my_assembler 프로그램에서는 라인단위로 토큰 및 오브젝트 관리를 하고 있다.
 * ----------------------------------------------------------------------------------
 */
int token_parsing(char* str)
{
    int errno;
    token_table[token_line] = malloc(sizeof(struct token_unit)); // 동적메모리 할당

    if (str == NULL) // 인자가 비어있으면
        errno = -1; // 음수 리턴
    else // 비어있지 않다면
    {
        char* s = (char*)malloc(sizeof(char) * (strlen(str) + 1)); //새로운 포인터 만들어서 동적메모리 할당
        strcpy(s, str); // 만든 포인터에 인자값 복사
        char* sArr[4] = { NULL, }; // 자른 토큰 넣을 배열
        int j = 0; //배열 이동할때 쓸 변수 초기화
        if (s[0] != '\t') // label이 있다면
        {
            char* ptr = strtok(s, "\t\n"); // s에 있는 소스코드를 tab과 줄바꿈 기준으로 토큰 잘라서 ptr 포인터로 위치 가리키기
            while (ptr != NULL) // 한 줄에 있는 모든 소스코드를 토큰으로 자를 때까지 반복
            {
                sArr[j] = ptr; // 자른 토큰을 j번째 배열에 넣기
                j++; // 배열 이동 (다음 토큰으로 이동)
                ptr = strtok(NULL, "\t\n"); // 자른 토큰 다음부터 tab과 줄바꿈 기준으로 토큰 잘라서 ptr 포인터로 위치 가리키기
            }
            token_table[token_line]->label = sArr[0]; // 첫번째 토큰을 token_table의 label에 넣기
            token_table[token_line]->operator = sArr[1]; // 두번째 토큰을 token_table의 operator에 넣기
            *token_table[token_line]->operand = sArr[2]; // 세번째 토큰을 token_table의 operand에 넣기
            token_table[token_line]->comment = sArr[3]; // 네번째 토큰을 token_table의 comment에 넣기
        }
        else // label이 없다면
        {
            if (s[1] != '\t') // operator가 있다면
            {
                char* ptr = strtok(s, "\t\n"); // s에 있는 소스코드를 tab과 줄바꿈 기준으로 토큰 잘라서 ptr 포인터로 위치 가리키기
                while (ptr != NULL) // 한 줄에 있는 모든 소스코드를 토큰으로 자를 때까지 반복
                {
                    sArr[j] = ptr; // 자른 토큰을 j번째 배열에 넣기
                    j++; // 배열 이동 (다음 토큰으로 이동)
                    ptr = strtok(NULL, "\t\n"); // 자른 토큰 다음부터 tab과 줄바꿈 기준으로 토큰 잘라서 ptr 포인터로 위치 가리키기
                }
                token_table[token_line]->label = NULL; // label이 없으니까 token_table의 label에 NULL값 넣기
                token_table[token_line]->operator = sArr[0]; // 첫번째 토큰을 token_table의 operator에 넣기
                *token_table[token_line]->operand = sArr[1]; // 두번째 토큰을 token_table의 operand에 넣기
                token_table[token_line]->comment = sArr[2]; // 세번째 토큰을 token_table의 comment에 넣기
            }
            else // operator가 없다면
            {
                char* ptr = strtok(s, "\t\n"); // s에 있는 소스코드를 tab과 줄바꿈 기준으로 토큰 잘라서 ptr 포인터로 위치 가리키기
                while (ptr != NULL) // 한 줄에 있는 모든 소스코드를 토큰으로 자를 때까지 반복
                {
                    sArr[j] = ptr; // 자른 토큰을 j번째 배열에 넣기
                    j++; // 배열 이동 (다음 토큰으로 이동)
                    ptr = strtok(NULL, "\t\n"); // 자른 토큰 다음부터 tab과 줄바꿈 기준으로 토큰 잘라서 ptr 포인터로 위치 가리키기
                }
                token_table[token_line]->label = NULL; // label이 없으니까 token_table의 label에 NULL값 넣기
                token_table[token_line]->operator = NULL; // operator가 없으니까 token_table의 operator에 NULL값 넣기
                *token_table[token_line]->operand = sArr[0]; // 첫번째 토큰을 token_table의 operand에 넣기
                token_table[token_line]->comment = sArr[1]; // 두번째 토큰을 token_table의 comment에 넣기
            }
        }
        errno = 0; // 정상종료
    }
    return errno;
}

/* ----------------------------------------------------------------------------------
 * 설명 : 입력 문자열이 기계어 코드인지를 검사하는 함수이다.
 * 매계 : 토큰 단위로 구분된 문자열
 * 반환 : 정상종료 = 기계어 테이블 인덱스, 에러 < 0
 * 주의 :
 *
 * ----------------------------------------------------------------------------------
 */
int search_opcode(char* str)
{
    int i = 0; // 인덱스 저장할 변수 초기화
    if (str[0] == '+') { // 4형식 operator라면(operator가 +로 시작한다면)
        while (inst_table[i] != NULL) // inst_table의 끝까지 반복
        {
            if (strcmp(inst_table[i]->name, str + 1) == 0) //+를 제외하고 그 다음 문자열과 inst_table에 있는 name 을 비교해서 같다면
            {
                return i; //인덱스 리턴
            }
            else //같지 않다면
            {
                i++; //다음 인덱스로 이동
            }
        }
        return -1; //끝까지 반복했는데 없다면 음수 리턴
    }
    else // 4형식 operator가 아니라면(operator가 +로 시작하지 않는다면)
    {
        while (inst_table[i] != NULL) // inst_table의 끝까지 반복
        {
            if (strcmp(inst_table[i]->name, str) == 0) //문자열과 inst_table에 있는 name 을 비교해서 같다면
            {
                return i; //인덱스 리턴
            }
            else //같지 않다면
            {
                i++; //다음 인덱스로 이동
            }
        }
        return -1; //끝까지 반복했는데 없다면 음수 리턴
    }
}

/* ----------------------------------------------------------------------------------
* 설명 : 어셈블리 코드를 위한 패스1과정을 수행하는 함수이다.
*		   패스1에서는..
*		   1. 프로그램 소스를 스캔하여 해당하는 토큰단위로 분리하여 프로그램 라인별 토큰
*		   테이블을 생성한다.
*
* 매계 : 없음
* 반환 : 정상 종료 = 0 , 에러 = < 0
* 주의 : 현재 초기 버전에서는 에러에 대한 검사를 하지 않고 넘어간 상태이다.
*	  따라서 에러에 대한 검사 루틴을 추가해야 한다.
*
* -----------------------------------------------------------------------------------
*/
static int assem_pass1(void)
{
    int errno;
    int i = 1; // 리터럴 순서 관리하는 변수
    char str[10]; // 리터럴에 들어갈 operand 넣어두는 배열
    for (token_line = 0; token_line < line_num; token_line++) //input_data의 끝까지 반복
    {
        token_parsing(input_data[token_line]); //input_data의 소스코드 한줄에서 토큰 파싱
        if (token_table[token_line]->operator != NULL) { //operator가 있다면
            if (strcmp(token_table[token_line]->operator, "START") == 0) { // operator가 START라면
                locctr = atoi(*token_table[token_line]->operand); // locctr에 시작주소(START의 operand를 숫자로 변환한 값) 넣기
                sym_table[token_line].addr = locctr; //심볼 테이블 주소에 locctr넣기 (심볼테이블 주소는 전역변수로 해당 operator의 주소를 나타내는 변수로 사용됨)
                sym_table[token_line + 1].addr = locctr; //START 다음 operator도 같은 주소 넣기 (START는 크기가 없는 명령어)
            }
            else if (strcmp(token_table[token_line]->operator, "CSECT") == 0) { // operator가 CSECT라면
                locctr = 0; // 주소 초기화 (새로운 섹션 시작)
                sym_table[token_line].addr = locctr; // 초기화한 주소를 심볼테이블 주소에 넣기
                sym_table[token_line + 1].addr = locctr; //다음 operator도 같은 주소 넣기 (CSECT는 크기가 없는 명령어)
            }
            else if (search_opcode(token_table[token_line]->operator) >= 0) { // inst_table에 있는 명령어라면
                if (strcmp(inst_table[search_opcode(token_table[token_line]->operator)]->format, "2") == 0) { // 4형식 명령어라면
                    locctr += 2; // 명령어 크기는 2이므로 주소에 2 더하기
                    sym_table[token_line + 1].addr = locctr; // 다음 주소에 명령어 크기만큼 커진 값 넣기
                }
                else if (strncmp(token_table[token_line]->operator, "+", 1) == 0) { // 4형식 명령어라면 (명령어가 +로 시작한다면)
                    locctr += 4; // 명령어 크기는 4이므로 주소에 4 더하기
                    sym_table[token_line + 1].addr = locctr; // 다음 주소에 명령어 크기만큼 커진 값 넣기
                }
                else { // 3형식 명령어라면
                    locctr += 3; // 명령어 크기는 3이므로 주소에 3 더하기 
                    sym_table[token_line + 1].addr = locctr; // 다음 주소에 명령어 크기만큼 커진 값 넣기
                }
            }
            else if (strcmp(token_table[token_line]->operator, "WORD") == 0) { // operator가 WORD라면
                locctr += 3; // 1WORD는 3bytes, 주소에 3 더하기 
                sym_table[token_line + 1].addr = locctr;  // 다음 주소에 명령어 크기만큼 커진 값 넣기
            }
            else if (strcmp(token_table[token_line]->operator, "RESW") == 0) { // operator가 RESW라면
                locctr = locctr + 3 * atoi(*token_table[token_line]->operand); // 주소는 WORD 개수(RESW의 operand를 숫자로 변환한 값)와 WORD크기(3)를 곱한걸 더하기
                sym_table[token_line + 1].addr = locctr;  // 다음 주소에 명령어 크기만큼 커진 값 넣기
            }
            else if (strcmp(token_table[token_line]->operator, "RESB") == 0) { // operator가 RESB라면
                locctr = locctr + atoi(*token_table[token_line]->operand); // 주소는 BYTE 개수(RESB의 operand를 숫자로 변환한 값)를 더하기
                sym_table[token_line + 1].addr = locctr;  // 다음 주소에 명령어 크기만큼 커진 값 넣기
            }
            else if (strcmp(token_table[token_line]->operator, "BYTE") == 0) { // operator가 BYTE라면
                if (strncmp(*token_table[token_line]->operand, "C", 1) == 0) { // operand가 C로 시작한다면
                    locctr = locctr + strlen(*token_table[token_line]->operand) - 3; // 문자의 길이(operand에서 C와 따옴표(3)를 뺀 길이)만큼 더하기
                    sym_table[token_line + 1].addr = locctr; // 다음 주소에 명령어 크기만큼 커진 값 넣기
                }
                else if (strncmp(*token_table[token_line]->operand, "X", 1) == 0) { // operand가 X로 시작한다면
                    locctr = locctr + (strlen(*token_table[token_line]->operand) - 3) / 2; // 문자의 길이(operand에서 X와 따옴표(3)를 뺀 길이)를 2로 나눈 만큼 더하기
                    sym_table[token_line + 1].addr = locctr; // 다음 주소에 명령어 크기만큼 커진 값 넣기
                }
            }
            else if (strcmp(token_table[token_line]->operator, "LTORG") == 0) { // operator가 LTORG라면 (그동안 나온 리터럴로 주소값 계산 해야됨)
                if (strncmp(str + 1, "C", 1) == 0) { // 리터럴에 들어간 operand의 "=" 다음 문자가 C라면
                    locctr = locctr + strlen(literal_table[i].literal); // 해당 리터럴의 길이만큼 더하기
                    sym_table[token_line + 1].addr = locctr; // 다음 주소에 명령어 크기만큼 커진 값 넣기
                    literal_table[i].addr = sym_table[token_line].addr; // 리터럴 테이블 주소값에 해당 주소 넣기
                }
                else if (strncmp(str + 1, "X", 1) == 0) {// 리터럴에 들어간 operand의 "=" 다음 문자가 X라면
                    locctr = locctr + (strlen(literal_table[i].literal)) / 2; // 해당 리터럴의 길이를 2로 나눈 만큼 더하기
                    sym_table[token_line + 1].addr = locctr; // 다음 주소에 명령어 크기만큼 커진 값 넣기
                    literal_table[i].addr = sym_table[token_line].addr; // 리터럴 테이블 주소값에 해당 주소 넣기
                }
            }
            else if (strcmp(token_table[token_line]->operator, "END") == 0) { // operator가 END라면 (그동안 나온 리터럴로 주소값 계산 하고 종료)
                if (strncmp(str+1, "C", 1) == 0) { // 리터럴에 들어간 operand의 "=" 다음 문자가 C라면
                    locctr = locctr + strlen(literal_table[i].literal); // 해당 리터럴의 길이만큼 더하기
                    sym_table[token_line + 1].addr = locctr; // 다음 주소에 명령어 크기만큼 커진 값 넣기
                    literal_table[i].addr = sym_table[token_line].addr; // 리터럴 테이블 주소값에 해당 주소 넣기
                }
                else if (strncmp(str + 1, "X", 1) == 0) { // 리터럴에 들어간 operand의 "=" 다음 문자가 X라면
                    locctr = locctr + (strlen(literal_table[i].literal)) / 2; // 해당 리터럴의 길이를 2로 나눈 만큼 더하기
                    sym_table[token_line + 1].addr = locctr; // 다음 주소에 명령어 크기만큼 커진 값 넣기
                    literal_table[i].addr = sym_table[token_line].addr; // 리터럴 테이블 주소값에 해당 주소 넣기
                }
                token_line++; // 추후 프로그램 길이를 구하기 위해 token_line을 하나 늘려주기
                break; // 종료
            }
            if (*token_table[token_line]->operand != NULL) { // operand가 있다면
                if (strncmp(*token_table[token_line]->operand, "=", 1) == 0) { // operand의 시작이 "="라면 
                    i = token_line; // 해당 라인을 i에 넣어주기
                    strcpy(str, *token_table[token_line]->operand); // str에 operand전체 넣기
                    strncpy(literal_table[i].literal, str + 3, strlen(*token_table[token_line]->operand) - 4); // 리터럴 테이블에 str에서 "=", 따옴표, C or X 빼고 넣기
                }
            }
        }
    }
    if (token_table == NULL) // token_table이 NULL이라면(파싱된 토큰이 없다면)
        errno = -1; //음수 리턴
    else //token_table에 제대로 파싱이 됐다면
        errno = 0; // 정상종료
    return errno;
}

/* ----------------------------------------------------------------------------------
* 설명 : 입력된 문자열의 이름을 가진 파일에 프로그램의 결과를 저장하는 함수이다.
*        여기서 출력되는 내용은 명령어 옆에 OPCODE가 기록된 표(과제 5번) 이다.
* 매계 : 생성할 오브젝트 파일명
* 반환 : 없음
* 주의 : 만약 인자로 NULL값이 들어온다면 프로그램의 결과를 표준출력으로 보내어
*        화면에 출력해준다.
*        또한 과제 5번에서만 쓰이는 함수이므로 이후의 프로젝트에서는 사용되지 않는다.
* -----------------------------------------------------------------------------------
*/
 /*void make_opcode_output(char *file_name)
 {
     FILE* file;
     file = fopen(file_name, "w"); //file_name을 쓰기 형식으로 열기
     int opcode = 0; //inst_data의 몇번째 줄에 있는 기계어인지 저장할 변수 초기화
     if (file != NULL) // 인자가 NULL값이 아니라면 (파일에 출력해야함)
     {
         for (int i = 0; i < token_line; i++) // token_table의 끝까지 반복
         {
             if (token_table[i]->operator != NULL) // operator가 있다면
             {
                 opcode = search_opcode(token_table[i]->operator); //해당 명령어가 inst_data의 몇번째 줄에 있는 명령어인지 opcode에 저장
                 if (token_table[i]->label == NULL) // label이 없다면
                 {
                     if (opcode == -1) // 명령어가 opcode가 없는 명령어라면 (ex.START, END)
                     {
                         if (*token_table[i]->operand == NULL) // operand가 없다면 (형식: XOXX)
                             fprintf(file, "\t%s\n", token_table[i]->operator); //operator만 출력
                         else // operand가 있다면 (형식: XOOX)
                             fprintf(file, "\t%s\t%s\n", token_table[i]->operator, *token_table[i]->operand); // operator와 operand 차례로 출력
                     }
                     else // 명령어가 opcode가 있는 명령어라면
                     {
                         if (inst_table[opcode]->opn == 0) // operand를 가지지 않는 명령어라면 (형식: XOXO)
                             fprintf(file, "\t%s\t\t\t%s\n", token_table[i]->operator, &inst_table[opcode]->op); // operator와 opcode 차례로 출력
                         else // operand를 가지는 명령어라면 (형식: XOOO)
                             fprintf(file, "\t%s\t%s\t\t%s\n", token_table[i]->operator, *token_table[i]->operand, &inst_table[opcode]->op); // operator와 operand, opcode 차례로 출력
                     }
                 }
                 else // label이 있다면
                 {
                     if (opcode == -1) // 명령어가 opcode가 없는 명령어라면 (ex.START, END)
                     {
                         if (*token_table[i]->operand == NULL) // operand가 없다면 (형식: OOXX)
                             fprintf(file, "%s\t%s\n", token_table[i]->label, token_table[i]->operator); // label과 operator 차례로 출력
                         else // operand가 있다면 (형식: OOOX)
                             fprintf(file, "%s\t%s\t%s\n", token_table[i]->label, token_table[i]->operator, *token_table[i]->operand); // label과 operator, operand 차례로 출력
                     }
                     else // 명령어가 opcode가 있는 명령어라면
                     {
                         if (inst_table[opcode]->opn == 0) // operand를 가지지 않는 명령어라면 (형식: OOXO)
                             fprintf(file, "%s\t%s\t\t\t%s\n", token_table[i]->label, token_table[i]->operator, &inst_table[opcode]->op); // label과 operator, opcode 차례로 출력
                         else  // operand를 가지는 명령어라면 (형식: OOOO)                       
                             fprintf(file, "%s\t%s\t%s\t\t%s\n", token_table[i]->label, token_table[i]->operator, *token_table[i]->operand, &inst_table[opcode]->op); // label과 operator, operand, opcode 차례로 출력
                     }
                 }
             }
             else  // operator가 없다면
             {
                 if (token_table[i]->label == NULL) // 빈 줄 읽었을 때
                     fprintf(file, ""); // 공백 출력
                 else // ‘.’만 있는 주석 Line 처리
                     fprintf(file, "%s\n", token_table[i]->label); //'.' 만 출력
             }
         }
     }
     else // 인자가 NULL값이라면 (화면에 출력해야함)
     {
         for (int i = 0; i < token_line; i++) // token_table의 끝까지 반복
         {
             if (token_table[i]->operator != NULL) // operator가 있다면
             {
                 opcode = search_opcode(token_table[i]->operator); //해당 명령어가 inst_data의 몇번째 줄에 있는 명령어인지 opcode에 저장
                 if (token_table[i]->label == NULL) // label이 없다면
                 {
                     if (opcode == -1) // 명령어가 opcode가 없는 명령어라면 (ex.START, END)
                     {
                         if (*token_table[i]->operand == NULL) // operand가 없다면 (형식: XOXX)
                             printf("\t%s\n", token_table[i]->operator); //operator만 출력
                         else // operand가 있다면 (형식: XOOX)
                             printf("\t%s\t%s\n", token_table[i]->operator, *token_table[i]->operand); // operator와 operand 차례로 출력
                     }
                     else // 명령어가 opcode가 있는 명령어라면
                     {
                         if (inst_table[opcode]->opn == 0) // operand를 가지지 않는 명령어라면 (형식: XOXO)
                             printf("\t%s\t\t\t%s\n", token_table[i]->operator, &inst_table[opcode]->op); // operator와 opcode 차례로 출력
                         else // operand를 가지는 명령어라면 (형식: XOOO)
                             printf("\t%s\t%s\t\t%s\n", token_table[i]->operator, *token_table[i]->operand, &inst_table[opcode]->op); // operator와 operand, opcode 차례로 출력
                     }
                 }
                 else // label이 있다면
                 {
                     if (opcode == -1) // 명령어가 opcode가 없는 명령어라면 (ex.START, END)
                     {
                         if (*token_table[i]->operand == NULL) // operand가 없다면 (형식: OOXX)
                             printf("%s\t%s\n", token_table[i]->label, token_table[i]->operator); // label과 operator 차례로 출력
                         else // operand가 있다면 (형식: OOOX)
                             printf("%s\t%s\t%s\n", token_table[i]->label, token_table[i]->operator, *token_table[i]->operand); // label과 operator, operand 차례로 출력
                     }
                     else // 명령어가 opcode가 있는 명령어라면
                     {
                         if (inst_table[opcode]->opn == 0) // operand를 가지지 않는 명령어라면 (형식: OOXO)
                             printf("%s\t%s\t\t\t%s\n", token_table[i]->label, token_table[i]->operator, &inst_table[opcode]->op); // label과 operator, opcode 차례로 출력
                         else  // operand를 가지는 명령어라면 (형식: OOOO)
                             printf("%s\t%s\t%s\t\t%s\n", token_table[i]->label, token_table[i]->operator, *token_table[i]->operand, &inst_table[opcode]->op); // label과 operator, operand, opcode 차례로 출력
                     }
                 }
             }
             else  // operator가 없다면
             {
                 if (token_table[i]->label == NULL) // 빈 줄 읽었을 때
                     printf(""); // 공백 출력
                 else // ‘.’만 있는 주석 Line 처리
                     printf("%s\n", token_table[i]->label); //'.' 만 출력
             }
         }
     }
 }*/

/* ----------------------------------------------------------------------------------
* 설명 : 입력된 문자열의 이름을 가진 파일에 프로그램의 결과를 저장하는 함수이다.
*        여기서 출력되는 내용은 SYMBOL별 주소값이 저장된 TABLE이다.
* 매계 : 생성할 오브젝트 파일명
* 반환 : 없음
* 주의 : 만약 인자로 NULL값이 들어온다면 프로그램의 결과를 표준출력으로 보내어
*        화면에 출력해준다.
*
* -----------------------------------------------------------------------------------
*/
void make_symtab_output(char* file_name)
{
    if (file_name != NULL) // 인자가 NULL값이 아니라면 (파일에 출력해야함)
    {
        FILE* file;
        file = fopen(file_name, "w"); //file_name을 쓰기 형식으로 열기
        for (int i = 0; i < token_line; i++) // token_table의 끝까지 반복
        {
            if (token_table[i]->label != NULL) { // label이 있다면 (심볼 테이블에 들어가야함)
                if (strncmp(token_table[i]->label, ".", 1) != 0) { // "."으로 시작하지 않는다면(중간에 불필요한 줄 처리)
                    strcpy(sym_table[i].symbol, token_table[i]->label); // label을 심볼 테이블에 넣기
                    if (strcmp(sym_table[i].symbol, "MAXLEN") == 0) // 심볼이 MAXLEN이라면
                        if (strcmp(sym_table[i - 1].symbol, "BUFEND") == 0) // 바로 윗줄에 BUFEND이 있다면
                            sym_table[i].addr = sym_table[i-1].addr - sym_table[i-2].addr; // MAXLEN주소는 BUFEND-BUFFER
                    fprintf(file, "%s\t%X\n", &sym_table[i].symbol, sym_table[i].addr); // 심볼이랑 주소 목록 출력 (주소는 pass1에서 넣었음)                        
                }
            }
        }
    }
    else // 인자가 NULL값이라면 (화면에 출력해야함)
    {
        for (int i = 0; i < token_line; i++) // token_table의 끝까지 반복
        {
            if (token_table[i]->label != NULL) { // label이 있다면 (심볼 테이블에 들어가야함)
                if (strncmp(token_table[i]->label, ".", 1) != 0) { // "."으로 시작하지 않는다면(중간에 불필요한 줄 처리)
                    strcpy(sym_table[i].symbol, token_table[i]->label); // label을 심볼 테이블에 넣기
                    if (strcmp(sym_table[i].symbol, "MAXLEN") == 0) // 심볼이 MAXLEN이라면
                        if (strcmp(sym_table[i - 1].symbol, "BUFEND") == 0) // 바로 윗줄에 BUFEND이 있다면
                            sym_table[i].addr = sym_table[i - 1].addr - sym_table[i - 2].addr; // MAXLEN주소는 BUFEND-BUFFER
                    printf("%s\t%X\n", &sym_table[i].symbol, sym_table[i].addr); // 심볼이랑 주소 목록 출력 (주소는 pass1에서 넣었음)
                }
            }
        }
        printf("\n"); // 화면출력할때 뒤에 출력되는 것과 구분하기 위해
    }
}

/* ----------------------------------------------------------------------------------
* 설명 : 입력된 문자열의 이름을 가진 파일에 프로그램의 결과를 저장하는 함수이다.
*        여기서 출력되는 내용은 LITERAL별 주소값이 저장된 TABLE이다.
* 매계 : 생성할 오브젝트 파일명
* 반환 : 없음
* 주의 : 만약 인자로 NULL값이 들어온다면 프로그램의 결과를 표준출력으로 보내어
*        화면에 출력해준다.
*
* -----------------------------------------------------------------------------------
*/
void make_literaltab_output(char* file_name)
{
    if (file_name != NULL) // 인자가 NULL값이 아니라면 (파일에 출력해야함)
    {
        FILE* file;
        file = fopen(file_name, "w"); //file_name을 쓰기 형식으로 열기
        for (int i = 0; i < token_line; i++) // token_table의 끝까지 반복
        {
            if (literal_table[i].addr != 0) { // 리터럴 주소가 0이 아니라면 (리터럴에 해당하는 주소만 pass1에서 넣었음)
                fprintf(file, "%s\t%X\n", literal_table[i].literal, literal_table[i].addr);// 리터럴이랑 주소 목록 출력
            }
        }
    }
    else // 인자가 NULL값이라면 (화면에 출력해야함)
    {
        for (int i = 0; i < token_line; i++) // token_table의 끝까지 반복
        {
            if (literal_table[i].addr != 0) { // 리터럴 주소가 0이 아니라면 (리터럴에 해당하는 주소만 pass1에서 넣었음)
                printf("%s\t%X\n", literal_table[i].literal, literal_table[i].addr);// 리터럴이랑 주소 목록 출력
            }
        }
        printf("\n"); // 화면출력할때 뒤에 출력되는 것과 구분하기 위해
    }
}

/* ----------------------------------------------------------------------------------
* 설명 : 어셈블리 코드를 기계어 코드로 바꾸기 위한 패스2 과정을 수행하는 함수이다.
*		   패스 2에서는 프로그램을 기계어로 바꾸는 작업은 라인 단위로 수행된다.
*		   다음과 같은 작업이 수행되어 진다.
*		   1. 실제로 해당 어셈블리 명령어를 기계어로 바꾸는 작업을 수행한다.
* 매계 : 없음
* 반환 : 정상종료 = 0, 에러발생 = < 0
* 주의 :
* -----------------------------------------------------------------------------------
*/
static int assem_pass2(void)
{
    int opcode = 0; //inst_data의 몇번째 줄에 있는 기계어인지 저장할 변수 초기화
    for (int i = 0; i < token_line; i++) // token_table의 끝까지 반복
    {
        token_table[i]->nixbpe = NULL; // nixbpe 넣을 변수 초기화
        if (token_table[i]->operator != NULL) // operator가 있다면
        {
            opcode = search_opcode(token_table[i]->operator); //해당 명령어가 inst_data의 몇번째 줄에 있는 명령어인지 opcode에 저장
            if (opcode > -1) //inst목록에 있는 operator만 (inst목록에 있는 명령어만 nixbpe 판단)
            {
                if (strcmp(inst_table[search_opcode(token_table[i]->operator)]->format, "2") == 0) { // 2형식이라면
                    strcpy(&token_table[i]->nixbpe, "000000"); //000000
                }
                else if (strncmp(token_table[i]->operator, "+", 1) == 0) { // 4형식이라면 b,p는 0, e는 1 ___001
                    if (strncmp(*token_table[i]->operand, "#", 1) == 0) { // operand 앞에 #붙는다면 n은 0, i는 1, 반복없음 x도 0 010001
                        strcpy(&token_table[i]->nixbpe, "010001"); //010001
                    }
                    else if (strncmp(*token_table[i]->operand, "@", 1) == 0) { // operand 앞에 @붙는다면 n은 1, i는 0, 반복없음 x도 0 100001
                            strcpy(&token_table[i]->nixbpe, "100001"); //100001
                    }
                    else { //operand 앞에 아무것도 안 붙으면 n,i는 1 11_001
                        if (strcmp(*token_table[i]->operand, "BUFFER,X") == 0) // operand가 BUFFER,X라면 반복있음 x는 1 111001
                            strcpy(&token_table[i]->nixbpe, "111001"); //111001
                        else // 반복없음 x는 0 110001
                            strcpy(&token_table[i]->nixbpe, "110001"); //110001
                    }
                }
                else { //3형식이라면 e는 0 _____0
                    if (strncmp(*token_table[i]->operand, "#", 1) == 0) { // operand 앞에 #붙는다면 n은 0, i는 1, 반복없음 x도 0, 컨트롤섹션은 전부 pc주소이므로 b는 0, p는 1 010000
                        strcpy(&token_table[i]->nixbpe, "010000"); //010000

                    }
                    else if (strncmp(*token_table[i]->operand, "@", 1) == 0) { // operand 앞에 @붙는다면 n은 1, i는 0, 반복없음 x도 0, 컨트롤섹션은 전부 pc주소이므로 b는 0, p는 1 100010
                        strcpy(&token_table[i]->nixbpe, "100010"); //100010
                    }
                    else { //operand 앞에 아무것도 안 붙으면 n,i는 1 11___0
                        if (strcmp(*token_table[i]->operand, "BUFFER,X") == 0) {// operand가 BUFFER,X라면 반복있음 x는 1, 컨트롤섹션은 전부 pc주소이므로 b는 0, p는 1 111010
                            strcpy(&token_table[i]->nixbpe, "111010"); //111010
                        }
                        else if (inst_table[opcode]->opn == 0) //반복없음 x는 0, operand를 가지지 않는 명령어라면 b,p는 0 110000
                            strcpy(&token_table[i]->nixbpe, "110000"); //110000
                        else {// 반복없음 x는 0, 컨트롤섹션은 전부 pc주소이므로 b는 0, p는 1 110010
                            strcpy(&token_table[i]->nixbpe, "110010"); //110010
                        }
                    }

                }
            }
        }
    }
}


/* ----------------------------------------------------------------------------------
* 설명 : 입력된 문자열의 이름을 가진 파일에 프로그램의 결과를 저장하는 함수이다.
*        여기서 출력되는 내용은 object code (프로젝트 1번) 이다.
* 매계 : 생성할 오브젝트 파일명
* 반환 : 없음
* 주의 : 만약 인자로 NULL값이 들어온다면 프로그램의 결과를 표준출력으로 보내어
*        화면에 출력해준다.
*
* -----------------------------------------------------------------------------------
*/
void make_objectcode_output(char* file_name)
{
    int opcode = 0; //inst_data의 몇번째 줄에 있는 기계어인지 저장할 변수 초기화
    int r1 = 0, r3 = 0, r2 = 0, mod = 0; // r1, r3는 REF 넣을 때 쓸 변수, r2, mod는 modification record 쓸 때 쓸 변수
    char* REF[5][10] = { NULL, }; // EXTREF에 있는 심볼 넣을 배열 각각 섹션번호, EXTREF에서 선언된 순서
    char* M[5][10] = { NULL, }; // modification record에 써야 하는 심볼 넣을 배열 각각 섹션번호, operand에서 나온 순서
    int* MA[5][10] = { NULL, }; // M에 있는 operand의 주소
    int PLen[5] = { NULL, }; // 각 프로그램 길이 넣을 배열 섹션번호
    int SLen[5][10] = { NULL, }; // 각 문장의 길이 넣을 배열 각각 섹션번호, 문장 순서
    int SAdd[5][10] = { NULL, }; // 각 문장 시작주소
    int l = 0, n = 0, len = 0, Sad = 0; // l은 SLen에 각 프로그램 길이를 넣을 때 쓰는 변수(문장 순서), n은 PLen에 각 프로그램 길이를 넣을 때 쓰는 변수(섹션번호), Sad는 각 문장 시작 주소
    int start = 0; // 시작 주소 넣을 배열 
    int sec = 0; // 섹션이 시작하는 라인을 넣을 변수
    for (int i = 0; i < token_line; i++) { // token_table의 끝까지 반복
        int ad = 0; // 각 섹션의 마지막 명령어가 몇번째 명령어인지 넣을 변수
        if (token_table[i]->operator != NULL) { // operator가 있다면
            if (strcmp(token_table[i]->operator, "START") == 0) { // START라면
                start = sym_table[i].addr; //프로그램의 시작 주소에 START의 주소 넣기
            }
            else if (strcmp(token_table[i]->operator, "CSECT") == 0 || strcmp(token_table[i]->operator, "END") == 0) { // 섹션이 끝난다면 (CSECT면 섹션이 바뀌고 END면 프로그램 종료)
                if (sym_table[i].addr != 0) { // 해당 주소가 0이 아니라면 (END라면)
                    PLen[n] = sym_table[i+1].addr - start; // 프로그램 길이는 해당주소의 다음 주소에서 시작주소를 뺀 값 (해당 주소는 그 명령어의 크기가 더해지지 않은 값이므로 다음 주소를 가져와야함)
                    Sad = start; // 첫 문장의 시작 주소는 프로그램의 시작주소
                }
                else { // 해당 주소가 0이라면 (CSECT라면)
                    if (strcmp(token_table[i - 1]->operator, "EQU") == 0) { // CSECT 이전 명령어가 EQU라면
                        for (int j = 1; j < i; j++) { // 계속 이전 명령어 검사
                            if (strcmp(token_table[i - j]->operator, "EQU") == 0) { // 이전 명령어가 또 EQU라면
                                PLen[n] = sym_table[i - j - 1].addr - start; // 프로그램 길이는 해당 주소의 이전 주소에서 시작 주소를 뺀 값
                                ad = i - j - 1; // 섹션의 마지막 명령어는 EQU 이전 명령어
                                start = 0; // 새로운 섹션이 시작했으므로 시작주소는 0으로 초기화
                            }
                            else break; // EQU가 나오지 않는다면 반복문 종료
                        }
                    }
                    else { // CSECT 이전 명령어가 EQU가 아니라면
                        PLen[n] = sym_table[i - 1].addr - start; // 프로그램 길이는 해당 주소의 이전 주소에서 시작 주소를 뺀 값
                        ad = i - 1; // 섹션의 마지막 명령어는 CSECT 이전 명령어
                        start = 0; // 새로운 섹션이 시작했으므로 시작주소는 0으로 초기화
                    }
                }
                if (search_opcode(token_table[ad]->operator) >= 0) { // ad번째 명령어가 inst목록에 있는 명령어라면
                    if (strcmp(inst_table[search_opcode(token_table[ad]->operator)]->format, "2") == 0) { // 2형식이라면
                        PLen[n] += 2; // 프로그램 길이에 2 더하기 
                    }
                    else if (strncmp(token_table[ad]->operator, "+", 1) == 0) { // 4형식이라면 (명령어가 +로 시작한다면)
                        PLen[n] += 4; // 프로그램 길이에 4 더하기
                    }
                    else { // 3형식이라면
                        PLen[n] += 3;  // 프로그램 길이에 3 더하기
                    }
                }
                else if (strcmp(token_table[ad]->operator, "WORD") == 0) { // ad번째 명령어가 WORD라면
                    PLen[n] += 3; // 프로그램 길이에 3 더하기 (1word = 3bytes)
                }
                else if (strcmp(token_table[ad]->operator, "RESW") == 0) { // ad번째 명령어가 RESW라면
                    PLen[n] = PLen[n] + 3 * atoi(*token_table[ad]->operand); // 프로그램 길이에 WORD 개수(RESW의 operand를 숫자로 변환한 값)와 WORD크기(3)를 곱한걸 더하기
                }
                else if (strcmp(token_table[ad]->operator, "RESB") == 0) { // ad번째 명령어가 RESB라면
                    PLen[n] = PLen[n] + atoi(*token_table[ad]->operand); // 프로그램 길이에 BYTE 개수(RESB의 operand를 숫자로 변환한 값)를 더하기
                }
                else if (strcmp(token_table[ad]->operator, "BYTE") == 0) { // ad번째 명령어가 BYTE라면
                    if (strncmp(*token_table[ad]->operand, "C", 1) == 0) { // operand가 C로 시작한다면
                        PLen[n] = PLen[n] + strlen(*token_table[ad]->operand) - 3; // 프로그램 길이에 문자의 길이(operand에서 C와 따옴표(3)를 뺀 길이)만큼 더하기
                    }
                    else if (strncmp(*token_table[ad]->operand, "X", 1) == 0) { // operand가 X로 시작한다면
                        PLen[n] = PLen[n] + (strlen(*token_table[ad]->operand) - 3) / 2; // 프로그램 길이에 문자의 길이(operand에서 X와 따옴표(3)를 뺀 길이)를 2로 나눈 만큼 더하기
                    }
                }
                SLen[n][l] = PLen[n] - Sad; // n번째 섹션에 l번째 문장 길이는 프로그램의 길이에서 Sad를 뺀 값
                SAdd[n][l] = Sad; // n번째 섹션에 l번째 문장의 시작주소는 Sad
                Sad = 0; // Sad 모두 사용했으니 초기화
                n++; // 다음 섹션으로
                l = 0; // 다음 섹션으로 넘어가므로 문장 순서도 초기화
            }
            if (strcmp(token_table[i]->operator, "RESW") == 0 || strcmp(token_table[i]->operator, "RESB") == 0 || strcmp(token_table[i]->operator, "EQU") == 0) { // 해당 라인이 object code가 없는 라인이라면 (명령어가 RESW or RESB or EQU)
                if (sym_table[i].addr != 0) { // 프로그램 시작 위치가 아니라면
                    if (strcmp(token_table[i-1]->operator, "RESW") != 0 && strcmp(token_table[i-1]->operator, "RESB") != 0 && strcmp(token_table[i-1]->operator, "EQU") != 0) { // 이전 라인이 object code가 있는 라인이라면 (명령어가 RESW, RESB, EQU 모두 아니라면)
                        // object code가 끊기는 부분에서 문장 끊기 위해
                        SLen[n][l] = sym_table[i].addr - Sad; // n번째 섹션에 l번째 문장 길이는 해당 주소에서 Sad를 뺀 값
                        SAdd[n][l] = Sad; // n번째 섹션에 l번째 문장의 시작주소는 Sad
                        l++; // 다음 문장으로
                        Sad = 0; // Sad 모두 사용했으니 초기화
                    }
                }
            }
            if (sym_table[i].addr > 0x1E) { // 해당 라인의 주소가 1E보다 크다면 (한 문장의 최대 길이는 1E)
                if (Sad == start) { // 문장 시작주소가 프로그램 시작주소와 같다면 (Sad 새로 지정해줘야 함)
                    if (strcmp(token_table[i]->operator, "RESW") != 0 && strcmp(token_table[i]->operator, "RESB") != 0 && strcmp(token_table[i]->operator, "EQU") != 0) { // 해당 라인이 object code가 있는 라인이라면 (명령어가 RESW, RESB, EQU 모두 아니라면)
                        if (search_opcode(token_table[i]->operator) < 0) { // 해당 라인의 명령어가 inst 목록에 없다면 (inst 목록에는 없지만 object code는 있는 명령어)
                            Sad = sym_table[i].addr; // 해당 라인의 주소를 Sad에 넣기
                        }
                        else { // 해당 라인의 명령어가 inst 목록에 없다면
                            Sad = sym_table[i - 1].addr; // 이전 라인의 주소를 Sad에 넣기
                        }
                    }
                }
            }
            else if (sym_table[i + 1].addr > 0x1E) { // 해당 라인의 주소는 1E보다 크지 않지만 다음 라인의 주소가 1E보다 크다면 (첫번째 문장의 마지막 명령어)
                SLen[n][l] = sym_table[i].addr - Sad; // n번째 섹션에 l번째 문장 길이는 해당 주소에서 Sad를 뺀 값
                l++; // 다음 문장으로
                Sad = 0; // Sad 모두 사용했으니 초기화
            }
        }
    }
    for (int i = 0; i < token_line; i++) // token_table의 끝까지 반복
    {
        char ob[9] = ""; // object code 넣을 배열
        int* pc = sym_table[i + 1].addr; // 다음 명령어의 주소를 넣을 변수
        int* ta = 0; // operand가 가리키는 주소를 넣을 변수
        char lit[MAX_INST] = ""; // 
        int r = 0; // operand가 EXTREF에 있는지 확인하기 위해 REF 배열의 섹션번호를 관리할 변수 
        for (int j = 0; j < token_line; j++) { // token_table의 끝까지 반복 (ta 찾는 반복문)
            if (token_table[i]->operator != NULL) { // operator가 있다면
                if (r1 == 0) { // 첫번째 섹션이라면
                    if (strcmp(token_table[j]->operator, "CSECT") == 0) { // 비교하는 operator가 CSECT라면 (처음부터 CSECT 전까지 비교했다면)
                        break; // 반복문 종료
                    }
                    if (strcmp(token_table[i]->operator, "CSECT") == 0) { // 원래 operator가 CSECT라면 (한 섹션의 끝까지 읽었다면)
                        sec = i; // 새로운 섹션이 시작하는 라인 번호를 sec에 넣기
                    }
                }
                else if (r1 != 0) { // 첫번째 섹션이 아니라면
                    if (j > sec) { // j가 계속 섹션만큼 증가하는 것 방지
                        j = j - sec - 1; // 더했던 만큼 빼줌 (원래 j값)
                    }
                    j = j + sec + 1; // j에 섹션만큼 더하면 CSECT이기 때문에 그 다음 가리킴
                    if (token_table[j]->operator != NULL) { // CSECT 다음 opeartor가 있다면
                        if (strcmp(token_table[j]->operator, "CSECT") == 0) { // 비교하는 operator가 CSECT라면 (섹션의 처음부터 CSECT 전까지 비교했다면)
                            break; // 반복문 종료
                        }
                    }
                    if (strcmp(token_table[i]->operator, "CSECT") == 0) { // 원래 operator가 CSECT라면 (한 섹션의 끝까지 읽었다면)
                        sec = i; // 새로운 섹션이 시작하는 라인 번호를 sec에 넣기
                    }                       
                }
            }
            if (*token_table[i]->operand != NULL) { // operand에 주소를 넣을 값이 있다면
                if (REF[r1][0] != NULL) { // 해당 섹션의 REF값이 있다면 (해당 섹션의 EXTREF 다음 줄부터 해당됨)
                    if (strncmp(*token_table[i]->operand, "@", 1) == 0) { // operand가 "@"로 시작한다면
                        if (strcmp((*token_table[i]->operand) + 1, &sym_table[j].symbol) == 0) // "@" 다음 글자부터 비교해서 심볼 중에 있다면
                        {
                            ta = sym_table[j].addr; // ta에 해당 심볼 주소를 넣기
                            break; // 반복문 종료
                        }
                    }
                    else if (strncmp(*token_table[i]->operand, "=", 1) == 0) { // operand가 "="로 시작한다면
                        strncpy(&lit, *token_table[i]->operand + 3, strlen(*token_table[i]->operand) - 4); // lit에 =, C or X, ' 이후부터 '전까지 넣기
                        if (strcmp(&lit, &literal_table[j].literal) == 0) // lit에 있는 문자열과 리터럴을 비교해서 같으면
                        {
                            if (literal_table[j].addr != 0) { // 해당 리터럴의 주소가 0이 아니라면
                                ta = literal_table[j].addr; // ta에 해당 리터럴 주소 넣기 
                                break; // 반복문 종료
                            }
                        }
                    }
                    else { // 둘 다 아니라면
                        if (strcmp(*token_table[i]->operand, &sym_table[j].symbol) == 0) // operand랑 심볼을 비교해서 같으면
                        {
                            for (int k = 0; k < r3; k++) { // EXTREF에 있는지 비교하기 위한 반복문
                                if (strncmp(*token_table[i]->operand, REF[r1][k], strlen(REF[r1][k])) == 0) { // operand를 REF에 있는 문자열의 길이만큼 문자열과 비교해서 REF에 있다면
                                    ta = 0; // ta에 0 넣기
                                    break; // 반복문 종료
                                }
                                else //REF에 없다면
                                    ta = sym_table[j].addr;// ta에 해당 심볼 주소를 넣기
                            }
                            break; // 반복문 종료
                        }
                    }
                }
            }
            else //operand가 없다면
                break; // 반복문 종료
        }
        if (token_table[i]->operator != NULL) // operator가 있다면
        {
            opcode = search_opcode(token_table[i]->operator); //해당 명령어가 inst_data의 몇번째 줄에 있는 명령어인지 opcode에 저장
            if (opcode < 0) { // operator가 inst목록에 없는 명령어라면
                if (strcmp(token_table[i]->operator, "EXTREF") == 0) { // operator가 EXTREF라면
                    char* ptr = strtok(*token_table[i]->operand, ", "); // operand에 있는 소스코드를 ","과 띄어쓰기 기준으로 토큰 잘라서 ptr 포인터로 위치 가리키기
                    char* sArr[10] = { NULL, }; // 자른 토큰 넣을 배열
                    while (ptr != NULL) // 한 줄에 있는 모든 operand를 토큰으로 자를 때까지 반복
                    {
                        sArr[r3] = ptr; // 자른 토큰을 r3번째 배열에 넣기
                        REF[r1][r3] = sArr[r3]; // REF 배열에 넣기
                        r3++; // 배열 이동 (다음 토큰으로 이동)
                        ptr = strtok(NULL, ", "); // 자른 토큰 다음부터 ","과 띄어쓰기 기준으로 토큰 잘라서 ptr 포인터로 위치 가리키기
                    }
                }
                else if (strcmp(token_table[i]->operator, "CSECT") == 0) { // operator가 CSECT라면
                    r1++; // 섹션번호 증가
                    r3 = 0; // REF 순서 변수 초기화
                }
                else if (strcmp(token_table[i]->operator, "LTORG") == 0 || strcmp(token_table[i]->operator, "END") == 0) { // operator가 LTORG 또는 END라면
                    for (int j = i; j >= 0; j--) { // 해당 라인부터 첫문장까지 반복
                        if (literal_table[j].addr != 0) { // 리터럴 주소가 0이 아니라면
                            if (strncmp(*token_table[j]->operand, "=C", 2) == 0) { // 해당 리터럴이 캐릭터라면
                                for (int k = 0; k < strlen(literal_table[j].literal); k++) { //리터럴 길이만큼
                                    sprintf(&lit, "%c", literal_table[j].literal[k]); // 한글자씩 lit에 넣기
                                    sprintf(&ob[2*k], "%X", *lit); // 한글자씩 아스키 코드로 변환
                                }
                            }
                            else if (strncmp(*token_table[j]->operand, "=X", 2) == 0) { // 해당 리터럴이 16진수 숫자라면
                                strcpy(&lit, literal_table[j].literal); // lit에 리터럴 넣기
                                strcpy(&ob, &lit); //ob에 lit 넣기
                            }
                            break; //반복문 종료
                        }
                    }
                }
                else if (strcmp(token_table[i]->operator, "BYTE") == 0) { // operator가 BYTE라면
                    if (strncmp(*token_table[i]->operand, "C", 1) == 0) { // operand가 C로 시작한다면 
                        for (int k = 0; k < strlen(*token_table[i]->operand) - 3; k++) { // 해당 문자열만 읽기 (C랑 따옴표 제외)
                            sprintf(&lit, "%c", *token_table[i]->operand + 2 + k); // 한글자씩 lit에 넣기
                            sprintf(&ob[2 * k], "%X", *lit); // 한글자씩 아스키 코드로 변환
                        }
                    }
                    if (strncmp(*token_table[i]->operand, "X", 1) == 0) { // operand가 X로 시작한다면 
                        strncpy(&ob, *token_table[i]->operand + 2, strlen(*token_table[i]->operand) - 3); // 해당 문자열만 읽어서 ob에 넣기 (X랑 따옴표 제외)
                    }
                }
                else if (strcmp(token_table[i]->operator, "WORD") == 0) { // operator가 WORD라면
                    sprintf(&ob, "%06X", ta); // ta를 6자리로 ob에 넣기
                }
            }
            else { // opeartor가 inst 목록에 있다면 
                int op2num = strtoul(&inst_table[opcode]->op, NULL, 16); // 문자열인 op를 숫자로 변환하여 저장
                if (&token_table[i]->nixbpe != NULL) { // nixbpe가 있다면
                    if (strncmp(&token_table[i]->nixbpe, "11", 2) == 0) { // 11로 시작한다면
                        strcpy(&ob, &inst_table[opcode]->op); // ob에 op넣기
                        sprintf(&ob, "%02X", op2num + 3); //숫자로 바뀐 op에 3을 더해서 ob에 넣기 (이진수 11은 3)
                    }
                    else if (strncmp(&token_table[i]->nixbpe, "10", 2) == 0) { // 10으로 시작한다면
                        strcpy(&ob, &inst_table[opcode]->op); // ob에 op넣기
                        sprintf(&ob, "%02X", op2num + 2); //숫자로 바뀐 op에 2를 더해서 ob에 넣기 (이진수 10은 2)

                    }
                    else if (strncmp(&token_table[i]->nixbpe, "01", 2) == 0) { // 01로 시작한다면
                        strcpy(&ob, &inst_table[opcode]->op); // ob에 op넣기
                        sprintf(&ob, "%02X", op2num + 1); //숫자로 바뀐 op에 1을 더해서 ob에 넣기 (이진수 01은 1)

                    }
                    else if (strncmp(&token_table[i]->nixbpe, "00", 2) == 0) { // 00으로 시작한다면
                        strcpy(&ob, &inst_table[opcode]->op); // ob에 op넣기
                        sprintf(&ob, "%02X", op2num); //숫자로 바뀐 op에 넣기
                    }
                    int bin[4]; // 나머지 xbpe를 이진수 계산하기 위한 배열
                    for (int n = 0; n < 4; n++) { // x, b, p, e를 각각 넣기 위해 4번 반복
                        if (strncmp(&token_table[i]->nixbpe + ((int)2 + (int)n), "0", 1) == 0) // ni를 빼기 위해 2를 더하고 나머지를 각각 0과 비교해서 같으면
                            bin[n] = 0; // 0 넣기
                        else // 다르면
                            bin[n] = 1; // 1 넣기
                    }
                    sprintf(&ob[2], "%X", bin[0] * 8 + bin[1] * 4 + bin[2] * 2 + bin[3] * 1); // ni가 들어있는 ob에 2번째에 계산한 값을 넣기
                    if (strncmp(token_table[i]->operator, "+", 1) == 0) { // 4형식이면
                        sprintf(&ob[3], "%05X", ta); // 주소를 5자리로 넣기
                    }
                    else { // 4형식이 아니라면
                        if (strncmp(*token_table[i]->operand, "#", 1) == 0) { // operand가 #으로 시작한다면
                            sprintf(&ob[3], "%03X", strtoul(*token_table[i]->operand + 1, NULL, 16)); // #뒤에 문자를 숫자로 바꿔서 3자리로 넣기
                        }
                        else if (strcmp(inst_table[search_opcode(token_table[i]->operator)]->format, "2") == 0) { //2형식이면
                            if (strncmp(*token_table[i]->operand, "A", 1) == 0) { // operand가 A로 시작한다면
                                strcpy(&ob[2], "0"); //0 넣기
                            }
                            else if (strncmp(*token_table[i]->operand, "X", 1) == 0) { // operand가 X로 시작한다면
                                strcpy(&ob[2], "1"); //1 넣기
                            }
                            else if (strncmp(*token_table[i]->operand, "S", 1) == 0) { // operand가 S로 시작한다면
                                strcpy(&ob[2], "4"); //4 넣기
                            }
                            else if (strncmp(*token_table[i]->operand, "T", 1) == 0) { // operand가 T로 시작한다면
                                strcpy(&ob[2], "5"); //5 넣기
                            }
                            if (strncmp(*token_table[i]->operand + 2, "A", 1) == 0) { // ,뒤에 레지스터가 A라면
                                strcpy(&ob[3], "0"); //0 넣기
                            }
                            else if (strncmp(*token_table[i]->operand + 2, "X", 1) == 0) { // ,뒤에 레지스터가 X라면
                                strcpy(&ob[3], "1"); //1 넣기
                            }
                            else if (strncmp(*token_table[i]->operand + 2, "S", 1) == 0) { // ,뒤에 레지스터가 S라면
                                strcpy(&ob[3], "4"); //4 넣기
                            }
                            else if (strncmp(*token_table[i]->operand + 2, "T", 1) == 0) { // ,뒤에 레지스터가 T라면
                                strcpy(&ob[3], "5"); //5 넣기
                            }
                            else // ,뒤에 레지스터가 없다면
                                strcpy(&ob[3], "0"); //0 넣기
                        }
                        else { //둘 다 아니라면
                            if (inst_table[opcode]->opn == 0) { // operand가 없다면
                                sprintf(&ob[3], "%03X", ta); // ta를 3자리로 넣기
                            }
                            else { //operand가 있다면
                                if ((int)ta >= (int)pc) { // ta가 pc보다 크거나 같다면
                                    sprintf(&ob[3], "%03X", (int)ta - (int)pc); // ta에서 pc를 빼서 세자리로 넣기
                                }
                                if ((int)ta < (int)pc) { // ta가 pc보다 작다면
                                    sprintf(&ob[3], "%03X", (int)ta + (0x1000 - (int)pc)); // pc를 1000에서 뺀 수를 ta에 더해서 세자리로 넣기
                                }
                            }
                        }
                    }
                }

            }          
            if (file_name != NULL) // 인자가 NULL값이 아니라면 (파일에 출력해야함)
            {
                FILE* file;
                if (i == 0) { // 파일을 처음 연다면
                    file = fopen(file_name, "w"); //file_name을 쓰기 형식으로 열기
                }
                else { // 처음 여는게 아니라면
                    file = fopen(file_name, "a"); // file_name을 이어쓰기 형식으로 열기
                }
                if (strcmp(token_table[i]->operator, "START") == 0) { // operator가 START라면
                    fprintf(file, "H%-6s%06X%06X\n", token_table[i]->label, sym_table[i].addr, PLen[0]); // Header 작성
                    start = sym_table[i].addr; // 시작 주소에 START의 주소를 넣기
                }
                if (strcmp(token_table[i]->operator, "EXTDEF") == 0) { // operator가 EXTDEF라면
                    char* ptr = strtok(*token_table[i]->operand, ", ");  // operand에 있는 소스코드를 ","과 띄어쓰기 기준으로 토큰 잘라서 ptr 포인터로 위치 가리키기
                    char* sArr[10] = { NULL, }; // 자른 토큰 넣을 배열
                    int* addr = 0; // 해당 심볼의 주소를 넣을 변수
                    int j = 0; // operand의 순서를 이동할 변수
                    while (ptr != NULL) // 한 줄에 있는 모든 operand를 토큰으로 자를 때까지 반복
                    {
                        sArr[j] = ptr; // 자른 토큰을 j번째 배열에 넣기
                        j++; // 배열 이동 (다음 토큰으로 이동)
                        ptr = strtok(NULL, ", "); // 자른 토큰 다음부터 ","과 띄어쓰기 기준으로 토큰 잘라서 ptr 포인터로 위치 가리키기
                    }
                    fprintf(file, "D"); // DEF 작성
                    for (int n = 0; n < j; n++) { // 0부터 읽은 개수만큼
                        for (int k = 1; k < token_line; k++) { // 심볼 테이블 끝까지 반복
                            if (strcmp(sArr[n], &sym_table[k].symbol) == 0) // 자른 토큰과 심볼을 비교해서 같다면
                            {
                                addr = sym_table[k].addr; // addr에 심볼 주소 넣기
                            }
                        }
                        fprintf(file, "%-6s%06X", sArr[n], addr); // DEF 작성
                    }
                    fprintf(file, "\n"); // 줄바꿈
                }
                if (strcmp(token_table[i]->operator, "EXTREF") == 0) { // operator가 EXTREF라면
                    fprintf(file, "R"); // REF 작성
                    for (int n = 0; n < r3; n++) { // 0부터 읽은 개수 만큼
                        fprintf(file, "%-6s", REF[r1][n]); // REF 작성
                    }
                }
                if (strcmp(token_table[i]->operator, "START") != 0 && strcmp(token_table[i]->operator, "EXTDEF") != 0 && strcmp(token_table[i]->operator, "EXTREF") != 0) { // operator가 START, EXTDEF, EXTREF 모두 아니라면
                    if (*token_table[i]->operand != NULL) { // operand가 있다면
                        n = 0; // n 초기화
                        while (REF[r2][n]) { // 해당 섹션의 REF가 저장되어 있다면
                            if (strncmp(*token_table[i]->operand, REF[r2][n], strlen(REF[r2][n])) == 0) // operand와 REF를 REF길이만큼 비교해서 같다면
                            {
                                M[r2][mod] = (char*) malloc(sizeof(char) * 10); // M 배열 동적할당
                                if (strcmp(&ob, "000000") == 0) { // 빈 주소가 6자리라면
                                    MA[r2][mod] = (int)sym_table[i].addr; // MA에 해당 심볼 주소 넣기
                                    sprintf(M[r2][mod], "%02X+%s", 6, REF[r2][n]); // M에 06+REF넣기
                                }
                                else { // 빈 주소가 5자리라면
                                    MA[r2][mod] = (int)sym_table[i].addr + (int)1; // MA에 해당 심볼 주소 +1 넣기
                                    sprintf(M[r2][mod], "%02X+%s", 5, REF[r2][n]); // M에 05+REF넣기
                                }                                
                                mod++; // 다음으로 이동
                                n++; // 다음으로 이동
                            }
                            else // 같지 않다면
                                n++; // 다음으로 이동
                        }
                    }
                }
                if (strncmp(&ob, "", 1) != 0) { //ob가 있다면
                    if (sym_table[i].addr == SAdd[r2][len]) { //현재주소와 해당 섹션의 시작 주소가 같다면
                        fprintf(file, "\nT%06X%02X", sym_table[i].addr, SLen[r2][len], r2, len); // TEXT 작성
                        len++; // 다음 문장으로 이동
                    }
                    fprintf(file, "%s", ob); // TEXT 작성
                }
                if (strcmp(token_table[i]->operator, "CSECT") == 0) { // operator가 CSECT라면
                    for (int n = 0; n < mod; n++) { //M에 넣은 REF만큼 반복
                        fprintf(file, "\nM%06X%-6s", MA[r2][n], M[r2][n]); //Modification 작성
                    }
                    if (r2 == 1) { // 1번째 섹션일때
                        memmove(M[r2][mod - 1]+2, "-BUFFER", 7); //마지막 Modification줄 복사해서 +BUFEND를 -BUFFER 변경
                        fprintf(file, "\nM%06X%-6s", MA[r2][mod-1], M[r2][mod-1]); //Modification 작성
                    }
                    fprintf(file, "\nE"); // End 작성
                    if (r2 == 0) { // 0번째 섹션일때
                        fprintf(file, "%06X", start); // 시작주소 넣기
                    }
                    r2++; //다음 섹션으로
                    fprintf(file, "\n\nH%-6s%06X%06X\n", token_table[i]->label, sym_table[i].addr, PLen[r2]); // Header 작성
                    mod = 0; // mod 초기화
                    len = 0; // len 초기화
                }  
                if (strcmp(token_table[i]->operator, "END") == 0) { // operator가 END라면
                    for (int n = 0; n < mod; n++) { //M에 넣은 REF만큼 반복
                        fprintf(file, "\nM%06X%-6s", MA[r2][n], M[r2][n]); //Modification 작성
                    }
                    fprintf(file, "\nE"); // End 작성
                    mod = 0; // mod 초기화
                }
                fclose(file); // 파일 닫기
            }
            else // 인자가 NULL값이라면 (화면에 출력해야함)
            {
            if (strcmp(token_table[i]->operator, "START") == 0) { // operator가 START라면
                printf("H%-6s%06X%06X\n", token_table[i]->label, sym_table[i].addr, PLen[0]); // Header 작성
                start = sym_table[i].addr; // 시작 주소에 START의 주소를 넣기
            }
            if (strcmp(token_table[i]->operator, "EXTDEF") == 0) { // operator가 EXTDEF라면
                char* ptr = strtok(*token_table[i]->operand, ", "); // operand에 있는 소스코드를 ","과 띄어쓰기 기준으로 토큰 잘라서 ptr 포인터로 위치 가리키기
                char* sArr[10] = { NULL, }; // 자른 토큰 넣을 배열
                int* addr = 0; // 해당 심볼의 주소를 넣을 변수
                int j = 0; // operand의 순서를 이동할 변수
                while (ptr != NULL) // 한 줄에 있는 모든 operand를 토큰으로 자를 때까지 반복
                {
                    sArr[j] = ptr; // 자른 토큰을 j번째 배열에 넣기
                    j++; // 배열 이동 (다음 토큰으로 이동)
                    ptr = strtok(NULL, ", "); // 자른 토큰 다음부터 ","과 띄어쓰기 기준으로 토큰 잘라서 ptr 포인터로 위치 가리키기
                }
                printf("D"); // DEF 작성
                for (int n = 0; n < j; n++) { // 0부터 읽은 개수만큼
                    for (int k = 1; k < token_line; k++) { // 심볼 테이블 끝까지 반복
                        if (strcmp(sArr[n], &sym_table[k].symbol) == 0) // 자른 토큰과 심볼을 비교해서 같다면
                        {
                            addr = sym_table[k].addr; // addr에 심볼 주소 넣기
                        }
                    }
                    printf("%-6s%06X", sArr[n], addr); // DEF 작성
                }
                printf("\n"); // 줄바꿈
            }
            if (strcmp(token_table[i]->operator, "EXTREF") == 0) { // operator가 EXTREF라면
                printf("R"); // REF 작성
                for (int n = 0; n < r3; n++) { // 0부터 읽은 개수 만큼
                    printf("%-6s", REF[r1][n]); // REF 작성
                }
            }
            if (strcmp(token_table[i]->operator, "START") != 0 && strcmp(token_table[i]->operator, "EXTDEF") != 0 && strcmp(token_table[i]->operator, "EXTREF") != 0) { // operator가 START, EXTDEF, EXTREF 모두 아니라면
                if (*token_table[i]->operand != NULL) { // operand가 있다면
                    n = 0;
                    while (REF[r2][n]) { // 해당 섹션의 REF가 저장되어 있다면
                        if (strncmp(*token_table[i]->operand, REF[r2][n], strlen(REF[r2][n])) == 0) // operand와 REF를 REF길이만큼 비교해서 같다면
                        {
                            M[r2][mod] = (char*)malloc(sizeof(char) * 10); // M 배열 동적할당
                            if (strcmp(&ob, "000000") == 0) { // 빈 주소가 6자리라면
                                MA[r2][mod] = (int)sym_table[i].addr; // MA에 해당 심볼 주소 넣기
                                sprintf(M[r2][mod], "%02X+%s", 6, REF[r2][n]); // M에 06+REF넣기
                            }
                            else { // 빈 주소가 5자리라면
                                MA[r2][mod] = (int)sym_table[i].addr + (int)1; // MA에 해당 심볼 주소 +1 넣기
                                sprintf(M[r2][mod], "%02X+%s", 5, REF[r2][n]); // M에 05+REF넣기
                            }
                            mod++; // 다음으로 이동
                            n++; // 다음으로 이동
                        }
                        else // 같지 않다면
                            n++; // 다음으로 이동
                    }
                }
            }
            if (strncmp(&ob, "", 1) != 0) { //ob가 있다면
                if (sym_table[i].addr == SAdd[r2][len]) { //현재주소와 해당 섹션의 시작 주소가 같다면
                    printf("\nT%06X%02X", sym_table[i].addr, SLen[r2][len], r2, len); // TEXT 작성
                    len++; // 다음 문장으로 이동
                }
                printf("%s", ob); // TEXT 작성
            }
            if (strcmp(token_table[i]->operator, "CSECT") == 0) { // operator가 CSECT라면
                for (int n = 0; n < mod; n++) { //M에 넣은 REF만큼 반복
                    printf("\nM%06X%-6s", MA[r2][n], M[r2][n]); //Modification 작성
                }
                if (r2 == 1) { // 1번째 섹션일때
                    memmove(M[r2][mod - 1] + 2, "-BUFFER", 7); //마지막 Modification줄 복사해서 +BUFEND를 -BUFFER 변경
                    printf("\nM%06X%-6s", MA[r2][mod - 1], M[r2][mod - 1]); //Modification 작성
                }
                printf("\nE"); // End 작성
                if (r2 == 0) { // 0번째 섹션일때
                    printf("%06X", start); // 시작주소 넣기
                }
                r2++; //다음 섹션으로
                printf("\n\nH%-6s%06X%06X\n", token_table[i]->label, sym_table[i].addr, PLen[r2]); // Header 작성
                mod = 0; // mod 초기화
                len = 0; // len 초기화
            }
            if (strcmp(token_table[i]->operator, "END") == 0) { // operator가 END라면
                for (int n = 0; n < mod; n++) { //M에 넣은 REF만큼 반복
                    printf("\nM%06X%-6s", MA[r2][n], M[r2][n]); //Modification 작성
                }
                printf("\nE"); // End 작성
                mod = 0; // mod 초기화
            }
            }
        }
    }
}
