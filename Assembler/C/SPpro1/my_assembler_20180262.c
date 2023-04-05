/*
 * ȭ�ϸ� : my_assembler_00000000.c
 * ��  �� : �� ���α׷��� SIC/XE �ӽ��� ���� ������ Assembler ���α׷��� ���η�ƾ����,
 * �Էµ� ������ �ڵ� ��, ��ɾ �ش��ϴ� OPCODE�� ã�� ����Ѵ�.
 * ���� ������ ���Ǵ� ���ڿ� "00000000"���� �ڽ��� �й��� �����Ѵ�.
 */

 /*
  *
  * ���α׷��� ����� �����Ѵ�.
  *
  */

#define _CRT_SECURE_NO_WARNINGS

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <fcntl.h>

  // ���ϸ��� "00000000"�� �ڽ��� �й����� ������ ��.
#include "my_assembler_20180262.h"

/* ----------------------------------------------------------------------------------
 * ���� : ����ڷ� ���� ����� ������ �޾Ƽ� ��ɾ��� OPCODE�� ã�� ����Ѵ�.
 * �Ű� : ���� ����, ����� ����
 * ��ȯ : ���� = 0, ���� = < 0
 * ���� : ���� ����� ���α׷��� ����Ʈ ������ �����ϴ� ��ƾ�� ������ �ʾҴ�.
 *		   ���� �߰������� �������� �ʴ´�.
 * ----------------------------------------------------------------------------------
 */
int main(int args, char* arg[])
{
    if (init_my_assembler() < 0)
    {
        printf("init_my_assembler: ���α׷� �ʱ�ȭ�� ���� �߽��ϴ�.\n");
        return -1;
    }

    if (assem_pass1() < 0)
    {
        printf("assem_pass1: �н�1 �������� �����Ͽ����ϴ�.  \n");
        return -1;
    }
    // make_opcode_output("output_20180262.txt");

    make_symtab_output(NULL); // ȭ�� ����� ���� NULL ����
    make_literaltab_output(NULL); // ȭ�� ����� ���� NULL ����
    if (assem_pass2() < 0)
    {
        printf("assem_pass2: �н�2 �������� �����Ͽ����ϴ�.  \n");
        return -1;
    }

    make_objectcode_output("output_20180262.txt");

    return 0;
}

/* ----------------------------------------------------------------------------------
 * ���� : ���α׷� �ʱ�ȭ�� ���� �ڷᱸ�� ���� �� ������ �д� �Լ��̴�.
 * �Ű� : ����
 * ��ȯ : �������� = 0 , ���� �߻� = -1
 * ���� : ������ ��ɾ� ���̺��� ���ο� �������� �ʰ� ������ �����ϰ� �ϱ�
 *		   ���ؼ� ���� ������ �����Ͽ� ���α׷� �ʱ�ȭ�� ���� ������ �о� �� �� �ֵ���
 *		   �����Ͽ���.
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
 * ���� : �ӽ��� ���� ��� �ڵ��� ������ �о� ���� ��� ���̺�(inst_table)��
 *        �����ϴ� �Լ��̴�.
 * �Ű� : ���� ��� ����
 * ��ȯ : �������� = 0 , ���� < 0
 * ���� : ���� ������� ������ �����Ӱ� �����Ѵ�. ���ô� ������ ����.
 *
 *	===============================================================================
 *		   | �̸� | ���� | ���� �ڵ� | ���۷����� ���� | NULL|
 *	===============================================================================
 *
 * ----------------------------------------------------------------------------------
 */
int init_inst_file(char* inst_file)
{
    FILE* file;
    int errno;
    int inst_index = 0; //�������� �ʱ�ȭ, �� �̵��� �� �� ����

    file = fopen(inst_file, "r"); // inst_file�� �б� �������� ����

    if (file == NULL) //������ ���ٸ�
        errno = -1; //���� ����
    else // ������ �ִٸ�
    {

        while (!feof(file)) // ���� ������ �ݺ�
        {
            inst_table[inst_index] = (inst*)malloc(sizeof(struct inst_unit)); // �����޸� �Ҵ�
            fscanf(file, "%s\t%s\t%s\t%d", inst_table[inst_index]->name, inst_table[inst_index]->format, &inst_table[inst_index]->op, &inst_table[inst_index]->opn); //tab�� �������� �о inst_table�� �� ����� �ֱ�
            inst_index++; //�����ٷ� �̵�
        }
        errno = 0; // ��������
        fclose(file); // ���� �ݱ�
    }
    return errno;
}

/* ----------------------------------------------------------------------------------
 * ���� : ����� �� �ҽ��ڵ带 �о� �ҽ��ڵ� ���̺�(input_data)�� �����ϴ� �Լ��̴�.
 * �Ű� : ������� �ҽ����ϸ�
 * ��ȯ : �������� = 0 , ���� < 0
 * ���� : ���δ����� �����Ѵ�.
 *
 * ----------------------------------------------------------------------------------
 */
int init_input_file(char* input_file)
{
    FILE* file;
    int errno;
    char* string[MAX_INST]; // ���� �ҽ��ڵ带 ������ �� �迭

    file = fopen(input_file, "r"); // input_file�� �б� �������� ����

    if (file == NULL) //������ ���ٸ�
        errno = -1; //���� ����
    else // ������ �ִٸ�
    {
        while (!feof(file)) // ���� ������ �ݺ�
        {
            fgets(string, MAX_INST, file); // �ҽ��ڵ带 ���δ����� �б�
            char* newdata = (char*)malloc(sizeof(char) * (strlen(string) + 1)); //���ο� �����Ϳ� �����޸� �Ҵ�
            strcpy(newdata, string); // �迭�� ����� �ҽ��ڵ带 �����ͷ� ����
            input_data[line_num] = newdata; //�ҽ��ڵ尡 ����� �����͸� input_data�� �ֱ�
            line_num++; //���� �ٷ� �̵�
        }
        errno = 0; // ��������
    }
    fclose(file); // ���� �ݱ�
    return errno;
}

/* ----------------------------------------------------------------------------------
 * ���� : �ҽ� �ڵ带 �о�� ��ū������ �м��ϰ� ��ū ���̺��� �ۼ��ϴ� �Լ��̴�.
 *        �н� 1�� ���� ȣ��ȴ�.
 * �Ű� : �Ľ��� ���ϴ� ���ڿ�
 * ��ȯ : �������� = 0 , ���� < 0
 * ���� : my_assembler ���α׷������� ���δ����� ��ū �� ������Ʈ ������ �ϰ� �ִ�.
 * ----------------------------------------------------------------------------------
 */
int token_parsing(char* str)
{
    int errno;
    token_table[token_line] = malloc(sizeof(struct token_unit)); // �����޸� �Ҵ�

    if (str == NULL) // ���ڰ� ���������
        errno = -1; // ���� ����
    else // ������� �ʴٸ�
    {
        char* s = (char*)malloc(sizeof(char) * (strlen(str) + 1)); //���ο� ������ ���� �����޸� �Ҵ�
        strcpy(s, str); // ���� �����Ϳ� ���ڰ� ����
        char* sArr[4] = { NULL, }; // �ڸ� ��ū ���� �迭
        int j = 0; //�迭 �̵��Ҷ� �� ���� �ʱ�ȭ
        if (s[0] != '\t') // label�� �ִٸ�
        {
            char* ptr = strtok(s, "\t\n"); // s�� �ִ� �ҽ��ڵ带 tab�� �ٹٲ� �������� ��ū �߶� ptr �����ͷ� ��ġ ����Ű��
            while (ptr != NULL) // �� �ٿ� �ִ� ��� �ҽ��ڵ带 ��ū���� �ڸ� ������ �ݺ�
            {
                sArr[j] = ptr; // �ڸ� ��ū�� j��° �迭�� �ֱ�
                j++; // �迭 �̵� (���� ��ū���� �̵�)
                ptr = strtok(NULL, "\t\n"); // �ڸ� ��ū �������� tab�� �ٹٲ� �������� ��ū �߶� ptr �����ͷ� ��ġ ����Ű��
            }
            token_table[token_line]->label = sArr[0]; // ù��° ��ū�� token_table�� label�� �ֱ�
            token_table[token_line]->operator = sArr[1]; // �ι�° ��ū�� token_table�� operator�� �ֱ�
            *token_table[token_line]->operand = sArr[2]; // ����° ��ū�� token_table�� operand�� �ֱ�
            token_table[token_line]->comment = sArr[3]; // �׹�° ��ū�� token_table�� comment�� �ֱ�
        }
        else // label�� ���ٸ�
        {
            if (s[1] != '\t') // operator�� �ִٸ�
            {
                char* ptr = strtok(s, "\t\n"); // s�� �ִ� �ҽ��ڵ带 tab�� �ٹٲ� �������� ��ū �߶� ptr �����ͷ� ��ġ ����Ű��
                while (ptr != NULL) // �� �ٿ� �ִ� ��� �ҽ��ڵ带 ��ū���� �ڸ� ������ �ݺ�
                {
                    sArr[j] = ptr; // �ڸ� ��ū�� j��° �迭�� �ֱ�
                    j++; // �迭 �̵� (���� ��ū���� �̵�)
                    ptr = strtok(NULL, "\t\n"); // �ڸ� ��ū �������� tab�� �ٹٲ� �������� ��ū �߶� ptr �����ͷ� ��ġ ����Ű��
                }
                token_table[token_line]->label = NULL; // label�� �����ϱ� token_table�� label�� NULL�� �ֱ�
                token_table[token_line]->operator = sArr[0]; // ù��° ��ū�� token_table�� operator�� �ֱ�
                *token_table[token_line]->operand = sArr[1]; // �ι�° ��ū�� token_table�� operand�� �ֱ�
                token_table[token_line]->comment = sArr[2]; // ����° ��ū�� token_table�� comment�� �ֱ�
            }
            else // operator�� ���ٸ�
            {
                char* ptr = strtok(s, "\t\n"); // s�� �ִ� �ҽ��ڵ带 tab�� �ٹٲ� �������� ��ū �߶� ptr �����ͷ� ��ġ ����Ű��
                while (ptr != NULL) // �� �ٿ� �ִ� ��� �ҽ��ڵ带 ��ū���� �ڸ� ������ �ݺ�
                {
                    sArr[j] = ptr; // �ڸ� ��ū�� j��° �迭�� �ֱ�
                    j++; // �迭 �̵� (���� ��ū���� �̵�)
                    ptr = strtok(NULL, "\t\n"); // �ڸ� ��ū �������� tab�� �ٹٲ� �������� ��ū �߶� ptr �����ͷ� ��ġ ����Ű��
                }
                token_table[token_line]->label = NULL; // label�� �����ϱ� token_table�� label�� NULL�� �ֱ�
                token_table[token_line]->operator = NULL; // operator�� �����ϱ� token_table�� operator�� NULL�� �ֱ�
                *token_table[token_line]->operand = sArr[0]; // ù��° ��ū�� token_table�� operand�� �ֱ�
                token_table[token_line]->comment = sArr[1]; // �ι�° ��ū�� token_table�� comment�� �ֱ�
            }
        }
        errno = 0; // ��������
    }
    return errno;
}

/* ----------------------------------------------------------------------------------
 * ���� : �Է� ���ڿ��� ���� �ڵ������� �˻��ϴ� �Լ��̴�.
 * �Ű� : ��ū ������ ���е� ���ڿ�
 * ��ȯ : �������� = ���� ���̺� �ε���, ���� < 0
 * ���� :
 *
 * ----------------------------------------------------------------------------------
 */
int search_opcode(char* str)
{
    int i = 0; // �ε��� ������ ���� �ʱ�ȭ
    if (str[0] == '+') { // 4���� operator���(operator�� +�� �����Ѵٸ�)
        while (inst_table[i] != NULL) // inst_table�� ������ �ݺ�
        {
            if (strcmp(inst_table[i]->name, str + 1) == 0) //+�� �����ϰ� �� ���� ���ڿ��� inst_table�� �ִ� name �� ���ؼ� ���ٸ�
            {
                return i; //�ε��� ����
            }
            else //���� �ʴٸ�
            {
                i++; //���� �ε����� �̵�
            }
        }
        return -1; //������ �ݺ��ߴµ� ���ٸ� ���� ����
    }
    else // 4���� operator�� �ƴ϶��(operator�� +�� �������� �ʴ´ٸ�)
    {
        while (inst_table[i] != NULL) // inst_table�� ������ �ݺ�
        {
            if (strcmp(inst_table[i]->name, str) == 0) //���ڿ��� inst_table�� �ִ� name �� ���ؼ� ���ٸ�
            {
                return i; //�ε��� ����
            }
            else //���� �ʴٸ�
            {
                i++; //���� �ε����� �̵�
            }
        }
        return -1; //������ �ݺ��ߴµ� ���ٸ� ���� ����
    }
}

/* ----------------------------------------------------------------------------------
* ���� : ����� �ڵ带 ���� �н�1������ �����ϴ� �Լ��̴�.
*		   �н�1������..
*		   1. ���α׷� �ҽ��� ��ĵ�Ͽ� �ش��ϴ� ��ū������ �и��Ͽ� ���α׷� ���κ� ��ū
*		   ���̺��� �����Ѵ�.
*
* �Ű� : ����
* ��ȯ : ���� ���� = 0 , ���� = < 0
* ���� : ���� �ʱ� ���������� ������ ���� �˻縦 ���� �ʰ� �Ѿ �����̴�.
*	  ���� ������ ���� �˻� ��ƾ�� �߰��ؾ� �Ѵ�.
*
* -----------------------------------------------------------------------------------
*/
static int assem_pass1(void)
{
    int errno;
    int i = 1; // ���ͷ� ���� �����ϴ� ����
    char str[10]; // ���ͷ��� �� operand �־�δ� �迭
    for (token_line = 0; token_line < line_num; token_line++) //input_data�� ������ �ݺ�
    {
        token_parsing(input_data[token_line]); //input_data�� �ҽ��ڵ� ���ٿ��� ��ū �Ľ�
        if (token_table[token_line]->operator != NULL) { //operator�� �ִٸ�
            if (strcmp(token_table[token_line]->operator, "START") == 0) { // operator�� START���
                locctr = atoi(*token_table[token_line]->operand); // locctr�� �����ּ�(START�� operand�� ���ڷ� ��ȯ�� ��) �ֱ�
                sym_table[token_line].addr = locctr; //�ɺ� ���̺� �ּҿ� locctr�ֱ� (�ɺ����̺� �ּҴ� ���������� �ش� operator�� �ּҸ� ��Ÿ���� ������ ����)
                sym_table[token_line + 1].addr = locctr; //START ���� operator�� ���� �ּ� �ֱ� (START�� ũ�Ⱑ ���� ��ɾ�)
            }
            else if (strcmp(token_table[token_line]->operator, "CSECT") == 0) { // operator�� CSECT���
                locctr = 0; // �ּ� �ʱ�ȭ (���ο� ���� ����)
                sym_table[token_line].addr = locctr; // �ʱ�ȭ�� �ּҸ� �ɺ����̺� �ּҿ� �ֱ�
                sym_table[token_line + 1].addr = locctr; //���� operator�� ���� �ּ� �ֱ� (CSECT�� ũ�Ⱑ ���� ��ɾ�)
            }
            else if (search_opcode(token_table[token_line]->operator) >= 0) { // inst_table�� �ִ� ��ɾ���
                if (strcmp(inst_table[search_opcode(token_table[token_line]->operator)]->format, "2") == 0) { // 4���� ��ɾ���
                    locctr += 2; // ��ɾ� ũ��� 2�̹Ƿ� �ּҿ� 2 ���ϱ�
                    sym_table[token_line + 1].addr = locctr; // ���� �ּҿ� ��ɾ� ũ�⸸ŭ Ŀ�� �� �ֱ�
                }
                else if (strncmp(token_table[token_line]->operator, "+", 1) == 0) { // 4���� ��ɾ��� (��ɾ +�� �����Ѵٸ�)
                    locctr += 4; // ��ɾ� ũ��� 4�̹Ƿ� �ּҿ� 4 ���ϱ�
                    sym_table[token_line + 1].addr = locctr; // ���� �ּҿ� ��ɾ� ũ�⸸ŭ Ŀ�� �� �ֱ�
                }
                else { // 3���� ��ɾ���
                    locctr += 3; // ��ɾ� ũ��� 3�̹Ƿ� �ּҿ� 3 ���ϱ� 
                    sym_table[token_line + 1].addr = locctr; // ���� �ּҿ� ��ɾ� ũ�⸸ŭ Ŀ�� �� �ֱ�
                }
            }
            else if (strcmp(token_table[token_line]->operator, "WORD") == 0) { // operator�� WORD���
                locctr += 3; // 1WORD�� 3bytes, �ּҿ� 3 ���ϱ� 
                sym_table[token_line + 1].addr = locctr;  // ���� �ּҿ� ��ɾ� ũ�⸸ŭ Ŀ�� �� �ֱ�
            }
            else if (strcmp(token_table[token_line]->operator, "RESW") == 0) { // operator�� RESW���
                locctr = locctr + 3 * atoi(*token_table[token_line]->operand); // �ּҴ� WORD ����(RESW�� operand�� ���ڷ� ��ȯ�� ��)�� WORDũ��(3)�� ���Ѱ� ���ϱ�
                sym_table[token_line + 1].addr = locctr;  // ���� �ּҿ� ��ɾ� ũ�⸸ŭ Ŀ�� �� �ֱ�
            }
            else if (strcmp(token_table[token_line]->operator, "RESB") == 0) { // operator�� RESB���
                locctr = locctr + atoi(*token_table[token_line]->operand); // �ּҴ� BYTE ����(RESB�� operand�� ���ڷ� ��ȯ�� ��)�� ���ϱ�
                sym_table[token_line + 1].addr = locctr;  // ���� �ּҿ� ��ɾ� ũ�⸸ŭ Ŀ�� �� �ֱ�
            }
            else if (strcmp(token_table[token_line]->operator, "BYTE") == 0) { // operator�� BYTE���
                if (strncmp(*token_table[token_line]->operand, "C", 1) == 0) { // operand�� C�� �����Ѵٸ�
                    locctr = locctr + strlen(*token_table[token_line]->operand) - 3; // ������ ����(operand���� C�� ����ǥ(3)�� �� ����)��ŭ ���ϱ�
                    sym_table[token_line + 1].addr = locctr; // ���� �ּҿ� ��ɾ� ũ�⸸ŭ Ŀ�� �� �ֱ�
                }
                else if (strncmp(*token_table[token_line]->operand, "X", 1) == 0) { // operand�� X�� �����Ѵٸ�
                    locctr = locctr + (strlen(*token_table[token_line]->operand) - 3) / 2; // ������ ����(operand���� X�� ����ǥ(3)�� �� ����)�� 2�� ���� ��ŭ ���ϱ�
                    sym_table[token_line + 1].addr = locctr; // ���� �ּҿ� ��ɾ� ũ�⸸ŭ Ŀ�� �� �ֱ�
                }
            }
            else if (strcmp(token_table[token_line]->operator, "LTORG") == 0) { // operator�� LTORG��� (�׵��� ���� ���ͷ��� �ּҰ� ��� �ؾߵ�)
                if (strncmp(str + 1, "C", 1) == 0) { // ���ͷ��� �� operand�� "=" ���� ���ڰ� C���
                    locctr = locctr + strlen(literal_table[i].literal); // �ش� ���ͷ��� ���̸�ŭ ���ϱ�
                    sym_table[token_line + 1].addr = locctr; // ���� �ּҿ� ��ɾ� ũ�⸸ŭ Ŀ�� �� �ֱ�
                    literal_table[i].addr = sym_table[token_line].addr; // ���ͷ� ���̺� �ּҰ��� �ش� �ּ� �ֱ�
                }
                else if (strncmp(str + 1, "X", 1) == 0) {// ���ͷ��� �� operand�� "=" ���� ���ڰ� X���
                    locctr = locctr + (strlen(literal_table[i].literal)) / 2; // �ش� ���ͷ��� ���̸� 2�� ���� ��ŭ ���ϱ�
                    sym_table[token_line + 1].addr = locctr; // ���� �ּҿ� ��ɾ� ũ�⸸ŭ Ŀ�� �� �ֱ�
                    literal_table[i].addr = sym_table[token_line].addr; // ���ͷ� ���̺� �ּҰ��� �ش� �ּ� �ֱ�
                }
            }
            else if (strcmp(token_table[token_line]->operator, "END") == 0) { // operator�� END��� (�׵��� ���� ���ͷ��� �ּҰ� ��� �ϰ� ����)
                if (strncmp(str+1, "C", 1) == 0) { // ���ͷ��� �� operand�� "=" ���� ���ڰ� C���
                    locctr = locctr + strlen(literal_table[i].literal); // �ش� ���ͷ��� ���̸�ŭ ���ϱ�
                    sym_table[token_line + 1].addr = locctr; // ���� �ּҿ� ��ɾ� ũ�⸸ŭ Ŀ�� �� �ֱ�
                    literal_table[i].addr = sym_table[token_line].addr; // ���ͷ� ���̺� �ּҰ��� �ش� �ּ� �ֱ�
                }
                else if (strncmp(str + 1, "X", 1) == 0) { // ���ͷ��� �� operand�� "=" ���� ���ڰ� X���
                    locctr = locctr + (strlen(literal_table[i].literal)) / 2; // �ش� ���ͷ��� ���̸� 2�� ���� ��ŭ ���ϱ�
                    sym_table[token_line + 1].addr = locctr; // ���� �ּҿ� ��ɾ� ũ�⸸ŭ Ŀ�� �� �ֱ�
                    literal_table[i].addr = sym_table[token_line].addr; // ���ͷ� ���̺� �ּҰ��� �ش� �ּ� �ֱ�
                }
                token_line++; // ���� ���α׷� ���̸� ���ϱ� ���� token_line�� �ϳ� �÷��ֱ�
                break; // ����
            }
            if (*token_table[token_line]->operand != NULL) { // operand�� �ִٸ�
                if (strncmp(*token_table[token_line]->operand, "=", 1) == 0) { // operand�� ������ "="��� 
                    i = token_line; // �ش� ������ i�� �־��ֱ�
                    strcpy(str, *token_table[token_line]->operand); // str�� operand��ü �ֱ�
                    strncpy(literal_table[i].literal, str + 3, strlen(*token_table[token_line]->operand) - 4); // ���ͷ� ���̺� str���� "=", ����ǥ, C or X ���� �ֱ�
                }
            }
        }
    }
    if (token_table == NULL) // token_table�� NULL�̶��(�Ľ̵� ��ū�� ���ٸ�)
        errno = -1; //���� ����
    else //token_table�� ����� �Ľ��� �ƴٸ�
        errno = 0; // ��������
    return errno;
}

/* ----------------------------------------------------------------------------------
* ���� : �Էµ� ���ڿ��� �̸��� ���� ���Ͽ� ���α׷��� ����� �����ϴ� �Լ��̴�.
*        ���⼭ ��µǴ� ������ ��ɾ� ���� OPCODE�� ��ϵ� ǥ(���� 5��) �̴�.
* �Ű� : ������ ������Ʈ ���ϸ�
* ��ȯ : ����
* ���� : ���� ���ڷ� NULL���� ���´ٸ� ���α׷��� ����� ǥ��������� ������
*        ȭ�鿡 ������ش�.
*        ���� ���� 5�������� ���̴� �Լ��̹Ƿ� ������ ������Ʈ������ ������ �ʴ´�.
* -----------------------------------------------------------------------------------
*/
 /*void make_opcode_output(char *file_name)
 {
     FILE* file;
     file = fopen(file_name, "w"); //file_name�� ���� �������� ����
     int opcode = 0; //inst_data�� ���° �ٿ� �ִ� �������� ������ ���� �ʱ�ȭ
     if (file != NULL) // ���ڰ� NULL���� �ƴ϶�� (���Ͽ� ����ؾ���)
     {
         for (int i = 0; i < token_line; i++) // token_table�� ������ �ݺ�
         {
             if (token_table[i]->operator != NULL) // operator�� �ִٸ�
             {
                 opcode = search_opcode(token_table[i]->operator); //�ش� ��ɾ inst_data�� ���° �ٿ� �ִ� ��ɾ����� opcode�� ����
                 if (token_table[i]->label == NULL) // label�� ���ٸ�
                 {
                     if (opcode == -1) // ��ɾ opcode�� ���� ��ɾ��� (ex.START, END)
                     {
                         if (*token_table[i]->operand == NULL) // operand�� ���ٸ� (����: XOXX)
                             fprintf(file, "\t%s\n", token_table[i]->operator); //operator�� ���
                         else // operand�� �ִٸ� (����: XOOX)
                             fprintf(file, "\t%s\t%s\n", token_table[i]->operator, *token_table[i]->operand); // operator�� operand ���ʷ� ���
                     }
                     else // ��ɾ opcode�� �ִ� ��ɾ���
                     {
                         if (inst_table[opcode]->opn == 0) // operand�� ������ �ʴ� ��ɾ��� (����: XOXO)
                             fprintf(file, "\t%s\t\t\t%s\n", token_table[i]->operator, &inst_table[opcode]->op); // operator�� opcode ���ʷ� ���
                         else // operand�� ������ ��ɾ��� (����: XOOO)
                             fprintf(file, "\t%s\t%s\t\t%s\n", token_table[i]->operator, *token_table[i]->operand, &inst_table[opcode]->op); // operator�� operand, opcode ���ʷ� ���
                     }
                 }
                 else // label�� �ִٸ�
                 {
                     if (opcode == -1) // ��ɾ opcode�� ���� ��ɾ��� (ex.START, END)
                     {
                         if (*token_table[i]->operand == NULL) // operand�� ���ٸ� (����: OOXX)
                             fprintf(file, "%s\t%s\n", token_table[i]->label, token_table[i]->operator); // label�� operator ���ʷ� ���
                         else // operand�� �ִٸ� (����: OOOX)
                             fprintf(file, "%s\t%s\t%s\n", token_table[i]->label, token_table[i]->operator, *token_table[i]->operand); // label�� operator, operand ���ʷ� ���
                     }
                     else // ��ɾ opcode�� �ִ� ��ɾ���
                     {
                         if (inst_table[opcode]->opn == 0) // operand�� ������ �ʴ� ��ɾ��� (����: OOXO)
                             fprintf(file, "%s\t%s\t\t\t%s\n", token_table[i]->label, token_table[i]->operator, &inst_table[opcode]->op); // label�� operator, opcode ���ʷ� ���
                         else  // operand�� ������ ��ɾ��� (����: OOOO)                       
                             fprintf(file, "%s\t%s\t%s\t\t%s\n", token_table[i]->label, token_table[i]->operator, *token_table[i]->operand, &inst_table[opcode]->op); // label�� operator, operand, opcode ���ʷ� ���
                     }
                 }
             }
             else  // operator�� ���ٸ�
             {
                 if (token_table[i]->label == NULL) // �� �� �о��� ��
                     fprintf(file, ""); // ���� ���
                 else // ��.���� �ִ� �ּ� Line ó��
                     fprintf(file, "%s\n", token_table[i]->label); //'.' �� ���
             }
         }
     }
     else // ���ڰ� NULL���̶�� (ȭ�鿡 ����ؾ���)
     {
         for (int i = 0; i < token_line; i++) // token_table�� ������ �ݺ�
         {
             if (token_table[i]->operator != NULL) // operator�� �ִٸ�
             {
                 opcode = search_opcode(token_table[i]->operator); //�ش� ��ɾ inst_data�� ���° �ٿ� �ִ� ��ɾ����� opcode�� ����
                 if (token_table[i]->label == NULL) // label�� ���ٸ�
                 {
                     if (opcode == -1) // ��ɾ opcode�� ���� ��ɾ��� (ex.START, END)
                     {
                         if (*token_table[i]->operand == NULL) // operand�� ���ٸ� (����: XOXX)
                             printf("\t%s\n", token_table[i]->operator); //operator�� ���
                         else // operand�� �ִٸ� (����: XOOX)
                             printf("\t%s\t%s\n", token_table[i]->operator, *token_table[i]->operand); // operator�� operand ���ʷ� ���
                     }
                     else // ��ɾ opcode�� �ִ� ��ɾ���
                     {
                         if (inst_table[opcode]->opn == 0) // operand�� ������ �ʴ� ��ɾ��� (����: XOXO)
                             printf("\t%s\t\t\t%s\n", token_table[i]->operator, &inst_table[opcode]->op); // operator�� opcode ���ʷ� ���
                         else // operand�� ������ ��ɾ��� (����: XOOO)
                             printf("\t%s\t%s\t\t%s\n", token_table[i]->operator, *token_table[i]->operand, &inst_table[opcode]->op); // operator�� operand, opcode ���ʷ� ���
                     }
                 }
                 else // label�� �ִٸ�
                 {
                     if (opcode == -1) // ��ɾ opcode�� ���� ��ɾ��� (ex.START, END)
                     {
                         if (*token_table[i]->operand == NULL) // operand�� ���ٸ� (����: OOXX)
                             printf("%s\t%s\n", token_table[i]->label, token_table[i]->operator); // label�� operator ���ʷ� ���
                         else // operand�� �ִٸ� (����: OOOX)
                             printf("%s\t%s\t%s\n", token_table[i]->label, token_table[i]->operator, *token_table[i]->operand); // label�� operator, operand ���ʷ� ���
                     }
                     else // ��ɾ opcode�� �ִ� ��ɾ���
                     {
                         if (inst_table[opcode]->opn == 0) // operand�� ������ �ʴ� ��ɾ��� (����: OOXO)
                             printf("%s\t%s\t\t\t%s\n", token_table[i]->label, token_table[i]->operator, &inst_table[opcode]->op); // label�� operator, opcode ���ʷ� ���
                         else  // operand�� ������ ��ɾ��� (����: OOOO)
                             printf("%s\t%s\t%s\t\t%s\n", token_table[i]->label, token_table[i]->operator, *token_table[i]->operand, &inst_table[opcode]->op); // label�� operator, operand, opcode ���ʷ� ���
                     }
                 }
             }
             else  // operator�� ���ٸ�
             {
                 if (token_table[i]->label == NULL) // �� �� �о��� ��
                     printf(""); // ���� ���
                 else // ��.���� �ִ� �ּ� Line ó��
                     printf("%s\n", token_table[i]->label); //'.' �� ���
             }
         }
     }
 }*/

/* ----------------------------------------------------------------------------------
* ���� : �Էµ� ���ڿ��� �̸��� ���� ���Ͽ� ���α׷��� ����� �����ϴ� �Լ��̴�.
*        ���⼭ ��µǴ� ������ SYMBOL�� �ּҰ��� ����� TABLE�̴�.
* �Ű� : ������ ������Ʈ ���ϸ�
* ��ȯ : ����
* ���� : ���� ���ڷ� NULL���� ���´ٸ� ���α׷��� ����� ǥ��������� ������
*        ȭ�鿡 ������ش�.
*
* -----------------------------------------------------------------------------------
*/
void make_symtab_output(char* file_name)
{
    if (file_name != NULL) // ���ڰ� NULL���� �ƴ϶�� (���Ͽ� ����ؾ���)
    {
        FILE* file;
        file = fopen(file_name, "w"); //file_name�� ���� �������� ����
        for (int i = 0; i < token_line; i++) // token_table�� ������ �ݺ�
        {
            if (token_table[i]->label != NULL) { // label�� �ִٸ� (�ɺ� ���̺� ������)
                if (strncmp(token_table[i]->label, ".", 1) != 0) { // "."���� �������� �ʴ´ٸ�(�߰��� ���ʿ��� �� ó��)
                    strcpy(sym_table[i].symbol, token_table[i]->label); // label�� �ɺ� ���̺� �ֱ�
                    if (strcmp(sym_table[i].symbol, "MAXLEN") == 0) // �ɺ��� MAXLEN�̶��
                        if (strcmp(sym_table[i - 1].symbol, "BUFEND") == 0) // �ٷ� ���ٿ� BUFEND�� �ִٸ�
                            sym_table[i].addr = sym_table[i-1].addr - sym_table[i-2].addr; // MAXLEN�ּҴ� BUFEND-BUFFER
                    fprintf(file, "%s\t%X\n", &sym_table[i].symbol, sym_table[i].addr); // �ɺ��̶� �ּ� ��� ��� (�ּҴ� pass1���� �־���)                        
                }
            }
        }
    }
    else // ���ڰ� NULL���̶�� (ȭ�鿡 ����ؾ���)
    {
        for (int i = 0; i < token_line; i++) // token_table�� ������ �ݺ�
        {
            if (token_table[i]->label != NULL) { // label�� �ִٸ� (�ɺ� ���̺� ������)
                if (strncmp(token_table[i]->label, ".", 1) != 0) { // "."���� �������� �ʴ´ٸ�(�߰��� ���ʿ��� �� ó��)
                    strcpy(sym_table[i].symbol, token_table[i]->label); // label�� �ɺ� ���̺� �ֱ�
                    if (strcmp(sym_table[i].symbol, "MAXLEN") == 0) // �ɺ��� MAXLEN�̶��
                        if (strcmp(sym_table[i - 1].symbol, "BUFEND") == 0) // �ٷ� ���ٿ� BUFEND�� �ִٸ�
                            sym_table[i].addr = sym_table[i - 1].addr - sym_table[i - 2].addr; // MAXLEN�ּҴ� BUFEND-BUFFER
                    printf("%s\t%X\n", &sym_table[i].symbol, sym_table[i].addr); // �ɺ��̶� �ּ� ��� ��� (�ּҴ� pass1���� �־���)
                }
            }
        }
        printf("\n"); // ȭ������Ҷ� �ڿ� ��µǴ� �Ͱ� �����ϱ� ����
    }
}

/* ----------------------------------------------------------------------------------
* ���� : �Էµ� ���ڿ��� �̸��� ���� ���Ͽ� ���α׷��� ����� �����ϴ� �Լ��̴�.
*        ���⼭ ��µǴ� ������ LITERAL�� �ּҰ��� ����� TABLE�̴�.
* �Ű� : ������ ������Ʈ ���ϸ�
* ��ȯ : ����
* ���� : ���� ���ڷ� NULL���� ���´ٸ� ���α׷��� ����� ǥ��������� ������
*        ȭ�鿡 ������ش�.
*
* -----------------------------------------------------------------------------------
*/
void make_literaltab_output(char* file_name)
{
    if (file_name != NULL) // ���ڰ� NULL���� �ƴ϶�� (���Ͽ� ����ؾ���)
    {
        FILE* file;
        file = fopen(file_name, "w"); //file_name�� ���� �������� ����
        for (int i = 0; i < token_line; i++) // token_table�� ������ �ݺ�
        {
            if (literal_table[i].addr != 0) { // ���ͷ� �ּҰ� 0�� �ƴ϶�� (���ͷ��� �ش��ϴ� �ּҸ� pass1���� �־���)
                fprintf(file, "%s\t%X\n", literal_table[i].literal, literal_table[i].addr);// ���ͷ��̶� �ּ� ��� ���
            }
        }
    }
    else // ���ڰ� NULL���̶�� (ȭ�鿡 ����ؾ���)
    {
        for (int i = 0; i < token_line; i++) // token_table�� ������ �ݺ�
        {
            if (literal_table[i].addr != 0) { // ���ͷ� �ּҰ� 0�� �ƴ϶�� (���ͷ��� �ش��ϴ� �ּҸ� pass1���� �־���)
                printf("%s\t%X\n", literal_table[i].literal, literal_table[i].addr);// ���ͷ��̶� �ּ� ��� ���
            }
        }
        printf("\n"); // ȭ������Ҷ� �ڿ� ��µǴ� �Ͱ� �����ϱ� ����
    }
}

/* ----------------------------------------------------------------------------------
* ���� : ����� �ڵ带 ���� �ڵ�� �ٲٱ� ���� �н�2 ������ �����ϴ� �Լ��̴�.
*		   �н� 2������ ���α׷��� ����� �ٲٴ� �۾��� ���� ������ ����ȴ�.
*		   ������ ���� �۾��� ����Ǿ� ����.
*		   1. ������ �ش� ����� ��ɾ ����� �ٲٴ� �۾��� �����Ѵ�.
* �Ű� : ����
* ��ȯ : �������� = 0, �����߻� = < 0
* ���� :
* -----------------------------------------------------------------------------------
*/
static int assem_pass2(void)
{
    int opcode = 0; //inst_data�� ���° �ٿ� �ִ� �������� ������ ���� �ʱ�ȭ
    for (int i = 0; i < token_line; i++) // token_table�� ������ �ݺ�
    {
        token_table[i]->nixbpe = NULL; // nixbpe ���� ���� �ʱ�ȭ
        if (token_table[i]->operator != NULL) // operator�� �ִٸ�
        {
            opcode = search_opcode(token_table[i]->operator); //�ش� ��ɾ inst_data�� ���° �ٿ� �ִ� ��ɾ����� opcode�� ����
            if (opcode > -1) //inst��Ͽ� �ִ� operator�� (inst��Ͽ� �ִ� ��ɾ nixbpe �Ǵ�)
            {
                if (strcmp(inst_table[search_opcode(token_table[i]->operator)]->format, "2") == 0) { // 2�����̶��
                    strcpy(&token_table[i]->nixbpe, "000000"); //000000
                }
                else if (strncmp(token_table[i]->operator, "+", 1) == 0) { // 4�����̶�� b,p�� 0, e�� 1 ___001
                    if (strncmp(*token_table[i]->operand, "#", 1) == 0) { // operand �տ� #�ٴ´ٸ� n�� 0, i�� 1, �ݺ����� x�� 0 010001
                        strcpy(&token_table[i]->nixbpe, "010001"); //010001
                    }
                    else if (strncmp(*token_table[i]->operand, "@", 1) == 0) { // operand �տ� @�ٴ´ٸ� n�� 1, i�� 0, �ݺ����� x�� 0 100001
                            strcpy(&token_table[i]->nixbpe, "100001"); //100001
                    }
                    else { //operand �տ� �ƹ��͵� �� ������ n,i�� 1 11_001
                        if (strcmp(*token_table[i]->operand, "BUFFER,X") == 0) // operand�� BUFFER,X��� �ݺ����� x�� 1 111001
                            strcpy(&token_table[i]->nixbpe, "111001"); //111001
                        else // �ݺ����� x�� 0 110001
                            strcpy(&token_table[i]->nixbpe, "110001"); //110001
                    }
                }
                else { //3�����̶�� e�� 0 _____0
                    if (strncmp(*token_table[i]->operand, "#", 1) == 0) { // operand �տ� #�ٴ´ٸ� n�� 0, i�� 1, �ݺ����� x�� 0, ��Ʈ�Ѽ����� ���� pc�ּ��̹Ƿ� b�� 0, p�� 1 010000
                        strcpy(&token_table[i]->nixbpe, "010000"); //010000

                    }
                    else if (strncmp(*token_table[i]->operand, "@", 1) == 0) { // operand �տ� @�ٴ´ٸ� n�� 1, i�� 0, �ݺ����� x�� 0, ��Ʈ�Ѽ����� ���� pc�ּ��̹Ƿ� b�� 0, p�� 1 100010
                        strcpy(&token_table[i]->nixbpe, "100010"); //100010
                    }
                    else { //operand �տ� �ƹ��͵� �� ������ n,i�� 1 11___0
                        if (strcmp(*token_table[i]->operand, "BUFFER,X") == 0) {// operand�� BUFFER,X��� �ݺ����� x�� 1, ��Ʈ�Ѽ����� ���� pc�ּ��̹Ƿ� b�� 0, p�� 1 111010
                            strcpy(&token_table[i]->nixbpe, "111010"); //111010
                        }
                        else if (inst_table[opcode]->opn == 0) //�ݺ����� x�� 0, operand�� ������ �ʴ� ��ɾ��� b,p�� 0 110000
                            strcpy(&token_table[i]->nixbpe, "110000"); //110000
                        else {// �ݺ����� x�� 0, ��Ʈ�Ѽ����� ���� pc�ּ��̹Ƿ� b�� 0, p�� 1 110010
                            strcpy(&token_table[i]->nixbpe, "110010"); //110010
                        }
                    }

                }
            }
        }
    }
}


/* ----------------------------------------------------------------------------------
* ���� : �Էµ� ���ڿ��� �̸��� ���� ���Ͽ� ���α׷��� ����� �����ϴ� �Լ��̴�.
*        ���⼭ ��µǴ� ������ object code (������Ʈ 1��) �̴�.
* �Ű� : ������ ������Ʈ ���ϸ�
* ��ȯ : ����
* ���� : ���� ���ڷ� NULL���� ���´ٸ� ���α׷��� ����� ǥ��������� ������
*        ȭ�鿡 ������ش�.
*
* -----------------------------------------------------------------------------------
*/
void make_objectcode_output(char* file_name)
{
    int opcode = 0; //inst_data�� ���° �ٿ� �ִ� �������� ������ ���� �ʱ�ȭ
    int r1 = 0, r3 = 0, r2 = 0, mod = 0; // r1, r3�� REF ���� �� �� ����, r2, mod�� modification record �� �� �� ����
    char* REF[5][10] = { NULL, }; // EXTREF�� �ִ� �ɺ� ���� �迭 ���� ���ǹ�ȣ, EXTREF���� ����� ����
    char* M[5][10] = { NULL, }; // modification record�� ��� �ϴ� �ɺ� ���� �迭 ���� ���ǹ�ȣ, operand���� ���� ����
    int* MA[5][10] = { NULL, }; // M�� �ִ� operand�� �ּ�
    int PLen[5] = { NULL, }; // �� ���α׷� ���� ���� �迭 ���ǹ�ȣ
    int SLen[5][10] = { NULL, }; // �� ������ ���� ���� �迭 ���� ���ǹ�ȣ, ���� ����
    int SAdd[5][10] = { NULL, }; // �� ���� �����ּ�
    int l = 0, n = 0, len = 0, Sad = 0; // l�� SLen�� �� ���α׷� ���̸� ���� �� ���� ����(���� ����), n�� PLen�� �� ���α׷� ���̸� ���� �� ���� ����(���ǹ�ȣ), Sad�� �� ���� ���� �ּ�
    int start = 0; // ���� �ּ� ���� �迭 
    int sec = 0; // ������ �����ϴ� ������ ���� ����
    for (int i = 0; i < token_line; i++) { // token_table�� ������ �ݺ�
        int ad = 0; // �� ������ ������ ��ɾ ���° ��ɾ����� ���� ����
        if (token_table[i]->operator != NULL) { // operator�� �ִٸ�
            if (strcmp(token_table[i]->operator, "START") == 0) { // START���
                start = sym_table[i].addr; //���α׷��� ���� �ּҿ� START�� �ּ� �ֱ�
            }
            else if (strcmp(token_table[i]->operator, "CSECT") == 0 || strcmp(token_table[i]->operator, "END") == 0) { // ������ �����ٸ� (CSECT�� ������ �ٲ�� END�� ���α׷� ����)
                if (sym_table[i].addr != 0) { // �ش� �ּҰ� 0�� �ƴ϶�� (END���)
                    PLen[n] = sym_table[i+1].addr - start; // ���α׷� ���̴� �ش��ּ��� ���� �ּҿ��� �����ּҸ� �� �� (�ش� �ּҴ� �� ��ɾ��� ũ�Ⱑ �������� ���� ���̹Ƿ� ���� �ּҸ� �����;���)
                    Sad = start; // ù ������ ���� �ּҴ� ���α׷��� �����ּ�
                }
                else { // �ش� �ּҰ� 0�̶�� (CSECT���)
                    if (strcmp(token_table[i - 1]->operator, "EQU") == 0) { // CSECT ���� ��ɾ EQU���
                        for (int j = 1; j < i; j++) { // ��� ���� ��ɾ� �˻�
                            if (strcmp(token_table[i - j]->operator, "EQU") == 0) { // ���� ��ɾ �� EQU���
                                PLen[n] = sym_table[i - j - 1].addr - start; // ���α׷� ���̴� �ش� �ּ��� ���� �ּҿ��� ���� �ּҸ� �� ��
                                ad = i - j - 1; // ������ ������ ��ɾ�� EQU ���� ��ɾ�
                                start = 0; // ���ο� ������ ���������Ƿ� �����ּҴ� 0���� �ʱ�ȭ
                            }
                            else break; // EQU�� ������ �ʴ´ٸ� �ݺ��� ����
                        }
                    }
                    else { // CSECT ���� ��ɾ EQU�� �ƴ϶��
                        PLen[n] = sym_table[i - 1].addr - start; // ���α׷� ���̴� �ش� �ּ��� ���� �ּҿ��� ���� �ּҸ� �� ��
                        ad = i - 1; // ������ ������ ��ɾ�� CSECT ���� ��ɾ�
                        start = 0; // ���ο� ������ ���������Ƿ� �����ּҴ� 0���� �ʱ�ȭ
                    }
                }
                if (search_opcode(token_table[ad]->operator) >= 0) { // ad��° ��ɾ inst��Ͽ� �ִ� ��ɾ���
                    if (strcmp(inst_table[search_opcode(token_table[ad]->operator)]->format, "2") == 0) { // 2�����̶��
                        PLen[n] += 2; // ���α׷� ���̿� 2 ���ϱ� 
                    }
                    else if (strncmp(token_table[ad]->operator, "+", 1) == 0) { // 4�����̶�� (��ɾ +�� �����Ѵٸ�)
                        PLen[n] += 4; // ���α׷� ���̿� 4 ���ϱ�
                    }
                    else { // 3�����̶��
                        PLen[n] += 3;  // ���α׷� ���̿� 3 ���ϱ�
                    }
                }
                else if (strcmp(token_table[ad]->operator, "WORD") == 0) { // ad��° ��ɾ WORD���
                    PLen[n] += 3; // ���α׷� ���̿� 3 ���ϱ� (1word = 3bytes)
                }
                else if (strcmp(token_table[ad]->operator, "RESW") == 0) { // ad��° ��ɾ RESW���
                    PLen[n] = PLen[n] + 3 * atoi(*token_table[ad]->operand); // ���α׷� ���̿� WORD ����(RESW�� operand�� ���ڷ� ��ȯ�� ��)�� WORDũ��(3)�� ���Ѱ� ���ϱ�
                }
                else if (strcmp(token_table[ad]->operator, "RESB") == 0) { // ad��° ��ɾ RESB���
                    PLen[n] = PLen[n] + atoi(*token_table[ad]->operand); // ���α׷� ���̿� BYTE ����(RESB�� operand�� ���ڷ� ��ȯ�� ��)�� ���ϱ�
                }
                else if (strcmp(token_table[ad]->operator, "BYTE") == 0) { // ad��° ��ɾ BYTE���
                    if (strncmp(*token_table[ad]->operand, "C", 1) == 0) { // operand�� C�� �����Ѵٸ�
                        PLen[n] = PLen[n] + strlen(*token_table[ad]->operand) - 3; // ���α׷� ���̿� ������ ����(operand���� C�� ����ǥ(3)�� �� ����)��ŭ ���ϱ�
                    }
                    else if (strncmp(*token_table[ad]->operand, "X", 1) == 0) { // operand�� X�� �����Ѵٸ�
                        PLen[n] = PLen[n] + (strlen(*token_table[ad]->operand) - 3) / 2; // ���α׷� ���̿� ������ ����(operand���� X�� ����ǥ(3)�� �� ����)�� 2�� ���� ��ŭ ���ϱ�
                    }
                }
                SLen[n][l] = PLen[n] - Sad; // n��° ���ǿ� l��° ���� ���̴� ���α׷��� ���̿��� Sad�� �� ��
                SAdd[n][l] = Sad; // n��° ���ǿ� l��° ������ �����ּҴ� Sad
                Sad = 0; // Sad ��� ��������� �ʱ�ȭ
                n++; // ���� ��������
                l = 0; // ���� �������� �Ѿ�Ƿ� ���� ������ �ʱ�ȭ
            }
            if (strcmp(token_table[i]->operator, "RESW") == 0 || strcmp(token_table[i]->operator, "RESB") == 0 || strcmp(token_table[i]->operator, "EQU") == 0) { // �ش� ������ object code�� ���� �����̶�� (��ɾ RESW or RESB or EQU)
                if (sym_table[i].addr != 0) { // ���α׷� ���� ��ġ�� �ƴ϶��
                    if (strcmp(token_table[i-1]->operator, "RESW") != 0 && strcmp(token_table[i-1]->operator, "RESB") != 0 && strcmp(token_table[i-1]->operator, "EQU") != 0) { // ���� ������ object code�� �ִ� �����̶�� (��ɾ RESW, RESB, EQU ��� �ƴ϶��)
                        // object code�� ����� �κп��� ���� ���� ����
                        SLen[n][l] = sym_table[i].addr - Sad; // n��° ���ǿ� l��° ���� ���̴� �ش� �ּҿ��� Sad�� �� ��
                        SAdd[n][l] = Sad; // n��° ���ǿ� l��° ������ �����ּҴ� Sad
                        l++; // ���� ��������
                        Sad = 0; // Sad ��� ��������� �ʱ�ȭ
                    }
                }
            }
            if (sym_table[i].addr > 0x1E) { // �ش� ������ �ּҰ� 1E���� ũ�ٸ� (�� ������ �ִ� ���̴� 1E)
                if (Sad == start) { // ���� �����ּҰ� ���α׷� �����ּҿ� ���ٸ� (Sad ���� ��������� ��)
                    if (strcmp(token_table[i]->operator, "RESW") != 0 && strcmp(token_table[i]->operator, "RESB") != 0 && strcmp(token_table[i]->operator, "EQU") != 0) { // �ش� ������ object code�� �ִ� �����̶�� (��ɾ RESW, RESB, EQU ��� �ƴ϶��)
                        if (search_opcode(token_table[i]->operator) < 0) { // �ش� ������ ��ɾ inst ��Ͽ� ���ٸ� (inst ��Ͽ��� ������ object code�� �ִ� ��ɾ�)
                            Sad = sym_table[i].addr; // �ش� ������ �ּҸ� Sad�� �ֱ�
                        }
                        else { // �ش� ������ ��ɾ inst ��Ͽ� ���ٸ�
                            Sad = sym_table[i - 1].addr; // ���� ������ �ּҸ� Sad�� �ֱ�
                        }
                    }
                }
            }
            else if (sym_table[i + 1].addr > 0x1E) { // �ش� ������ �ּҴ� 1E���� ũ�� ������ ���� ������ �ּҰ� 1E���� ũ�ٸ� (ù��° ������ ������ ��ɾ�)
                SLen[n][l] = sym_table[i].addr - Sad; // n��° ���ǿ� l��° ���� ���̴� �ش� �ּҿ��� Sad�� �� ��
                l++; // ���� ��������
                Sad = 0; // Sad ��� ��������� �ʱ�ȭ
            }
        }
    }
    for (int i = 0; i < token_line; i++) // token_table�� ������ �ݺ�
    {
        char ob[9] = ""; // object code ���� �迭
        int* pc = sym_table[i + 1].addr; // ���� ��ɾ��� �ּҸ� ���� ����
        int* ta = 0; // operand�� ����Ű�� �ּҸ� ���� ����
        char lit[MAX_INST] = ""; // 
        int r = 0; // operand�� EXTREF�� �ִ��� Ȯ���ϱ� ���� REF �迭�� ���ǹ�ȣ�� ������ ���� 
        for (int j = 0; j < token_line; j++) { // token_table�� ������ �ݺ� (ta ã�� �ݺ���)
            if (token_table[i]->operator != NULL) { // operator�� �ִٸ�
                if (r1 == 0) { // ù��° �����̶��
                    if (strcmp(token_table[j]->operator, "CSECT") == 0) { // ���ϴ� operator�� CSECT��� (ó������ CSECT ������ ���ߴٸ�)
                        break; // �ݺ��� ����
                    }
                    if (strcmp(token_table[i]->operator, "CSECT") == 0) { // ���� operator�� CSECT��� (�� ������ ������ �о��ٸ�)
                        sec = i; // ���ο� ������ �����ϴ� ���� ��ȣ�� sec�� �ֱ�
                    }
                }
                else if (r1 != 0) { // ù��° ������ �ƴ϶��
                    if (j > sec) { // j�� ��� ���Ǹ�ŭ �����ϴ� �� ����
                        j = j - sec - 1; // ���ߴ� ��ŭ ���� (���� j��)
                    }
                    j = j + sec + 1; // j�� ���Ǹ�ŭ ���ϸ� CSECT�̱� ������ �� ���� ����Ŵ
                    if (token_table[j]->operator != NULL) { // CSECT ���� opeartor�� �ִٸ�
                        if (strcmp(token_table[j]->operator, "CSECT") == 0) { // ���ϴ� operator�� CSECT��� (������ ó������ CSECT ������ ���ߴٸ�)
                            break; // �ݺ��� ����
                        }
                    }
                    if (strcmp(token_table[i]->operator, "CSECT") == 0) { // ���� operator�� CSECT��� (�� ������ ������ �о��ٸ�)
                        sec = i; // ���ο� ������ �����ϴ� ���� ��ȣ�� sec�� �ֱ�
                    }                       
                }
            }
            if (*token_table[i]->operand != NULL) { // operand�� �ּҸ� ���� ���� �ִٸ�
                if (REF[r1][0] != NULL) { // �ش� ������ REF���� �ִٸ� (�ش� ������ EXTREF ���� �ٺ��� �ش��)
                    if (strncmp(*token_table[i]->operand, "@", 1) == 0) { // operand�� "@"�� �����Ѵٸ�
                        if (strcmp((*token_table[i]->operand) + 1, &sym_table[j].symbol) == 0) // "@" ���� ���ں��� ���ؼ� �ɺ� �߿� �ִٸ�
                        {
                            ta = sym_table[j].addr; // ta�� �ش� �ɺ� �ּҸ� �ֱ�
                            break; // �ݺ��� ����
                        }
                    }
                    else if (strncmp(*token_table[i]->operand, "=", 1) == 0) { // operand�� "="�� �����Ѵٸ�
                        strncpy(&lit, *token_table[i]->operand + 3, strlen(*token_table[i]->operand) - 4); // lit�� =, C or X, ' ���ĺ��� '������ �ֱ�
                        if (strcmp(&lit, &literal_table[j].literal) == 0) // lit�� �ִ� ���ڿ��� ���ͷ��� ���ؼ� ������
                        {
                            if (literal_table[j].addr != 0) { // �ش� ���ͷ��� �ּҰ� 0�� �ƴ϶��
                                ta = literal_table[j].addr; // ta�� �ش� ���ͷ� �ּ� �ֱ� 
                                break; // �ݺ��� ����
                            }
                        }
                    }
                    else { // �� �� �ƴ϶��
                        if (strcmp(*token_table[i]->operand, &sym_table[j].symbol) == 0) // operand�� �ɺ��� ���ؼ� ������
                        {
                            for (int k = 0; k < r3; k++) { // EXTREF�� �ִ��� ���ϱ� ���� �ݺ���
                                if (strncmp(*token_table[i]->operand, REF[r1][k], strlen(REF[r1][k])) == 0) { // operand�� REF�� �ִ� ���ڿ��� ���̸�ŭ ���ڿ��� ���ؼ� REF�� �ִٸ�
                                    ta = 0; // ta�� 0 �ֱ�
                                    break; // �ݺ��� ����
                                }
                                else //REF�� ���ٸ�
                                    ta = sym_table[j].addr;// ta�� �ش� �ɺ� �ּҸ� �ֱ�
                            }
                            break; // �ݺ��� ����
                        }
                    }
                }
            }
            else //operand�� ���ٸ�
                break; // �ݺ��� ����
        }
        if (token_table[i]->operator != NULL) // operator�� �ִٸ�
        {
            opcode = search_opcode(token_table[i]->operator); //�ش� ��ɾ inst_data�� ���° �ٿ� �ִ� ��ɾ����� opcode�� ����
            if (opcode < 0) { // operator�� inst��Ͽ� ���� ��ɾ���
                if (strcmp(token_table[i]->operator, "EXTREF") == 0) { // operator�� EXTREF���
                    char* ptr = strtok(*token_table[i]->operand, ", "); // operand�� �ִ� �ҽ��ڵ带 ","�� ���� �������� ��ū �߶� ptr �����ͷ� ��ġ ����Ű��
                    char* sArr[10] = { NULL, }; // �ڸ� ��ū ���� �迭
                    while (ptr != NULL) // �� �ٿ� �ִ� ��� operand�� ��ū���� �ڸ� ������ �ݺ�
                    {
                        sArr[r3] = ptr; // �ڸ� ��ū�� r3��° �迭�� �ֱ�
                        REF[r1][r3] = sArr[r3]; // REF �迭�� �ֱ�
                        r3++; // �迭 �̵� (���� ��ū���� �̵�)
                        ptr = strtok(NULL, ", "); // �ڸ� ��ū �������� ","�� ���� �������� ��ū �߶� ptr �����ͷ� ��ġ ����Ű��
                    }
                }
                else if (strcmp(token_table[i]->operator, "CSECT") == 0) { // operator�� CSECT���
                    r1++; // ���ǹ�ȣ ����
                    r3 = 0; // REF ���� ���� �ʱ�ȭ
                }
                else if (strcmp(token_table[i]->operator, "LTORG") == 0 || strcmp(token_table[i]->operator, "END") == 0) { // operator�� LTORG �Ǵ� END���
                    for (int j = i; j >= 0; j--) { // �ش� ���κ��� ù������� �ݺ�
                        if (literal_table[j].addr != 0) { // ���ͷ� �ּҰ� 0�� �ƴ϶��
                            if (strncmp(*token_table[j]->operand, "=C", 2) == 0) { // �ش� ���ͷ��� ĳ���Ͷ��
                                for (int k = 0; k < strlen(literal_table[j].literal); k++) { //���ͷ� ���̸�ŭ
                                    sprintf(&lit, "%c", literal_table[j].literal[k]); // �ѱ��ھ� lit�� �ֱ�
                                    sprintf(&ob[2*k], "%X", *lit); // �ѱ��ھ� �ƽ�Ű �ڵ�� ��ȯ
                                }
                            }
                            else if (strncmp(*token_table[j]->operand, "=X", 2) == 0) { // �ش� ���ͷ��� 16���� ���ڶ��
                                strcpy(&lit, literal_table[j].literal); // lit�� ���ͷ� �ֱ�
                                strcpy(&ob, &lit); //ob�� lit �ֱ�
                            }
                            break; //�ݺ��� ����
                        }
                    }
                }
                else if (strcmp(token_table[i]->operator, "BYTE") == 0) { // operator�� BYTE���
                    if (strncmp(*token_table[i]->operand, "C", 1) == 0) { // operand�� C�� �����Ѵٸ� 
                        for (int k = 0; k < strlen(*token_table[i]->operand) - 3; k++) { // �ش� ���ڿ��� �б� (C�� ����ǥ ����)
                            sprintf(&lit, "%c", *token_table[i]->operand + 2 + k); // �ѱ��ھ� lit�� �ֱ�
                            sprintf(&ob[2 * k], "%X", *lit); // �ѱ��ھ� �ƽ�Ű �ڵ�� ��ȯ
                        }
                    }
                    if (strncmp(*token_table[i]->operand, "X", 1) == 0) { // operand�� X�� �����Ѵٸ� 
                        strncpy(&ob, *token_table[i]->operand + 2, strlen(*token_table[i]->operand) - 3); // �ش� ���ڿ��� �о ob�� �ֱ� (X�� ����ǥ ����)
                    }
                }
                else if (strcmp(token_table[i]->operator, "WORD") == 0) { // operator�� WORD���
                    sprintf(&ob, "%06X", ta); // ta�� 6�ڸ��� ob�� �ֱ�
                }
            }
            else { // opeartor�� inst ��Ͽ� �ִٸ� 
                int op2num = strtoul(&inst_table[opcode]->op, NULL, 16); // ���ڿ��� op�� ���ڷ� ��ȯ�Ͽ� ����
                if (&token_table[i]->nixbpe != NULL) { // nixbpe�� �ִٸ�
                    if (strncmp(&token_table[i]->nixbpe, "11", 2) == 0) { // 11�� �����Ѵٸ�
                        strcpy(&ob, &inst_table[opcode]->op); // ob�� op�ֱ�
                        sprintf(&ob, "%02X", op2num + 3); //���ڷ� �ٲ� op�� 3�� ���ؼ� ob�� �ֱ� (������ 11�� 3)
                    }
                    else if (strncmp(&token_table[i]->nixbpe, "10", 2) == 0) { // 10���� �����Ѵٸ�
                        strcpy(&ob, &inst_table[opcode]->op); // ob�� op�ֱ�
                        sprintf(&ob, "%02X", op2num + 2); //���ڷ� �ٲ� op�� 2�� ���ؼ� ob�� �ֱ� (������ 10�� 2)

                    }
                    else if (strncmp(&token_table[i]->nixbpe, "01", 2) == 0) { // 01�� �����Ѵٸ�
                        strcpy(&ob, &inst_table[opcode]->op); // ob�� op�ֱ�
                        sprintf(&ob, "%02X", op2num + 1); //���ڷ� �ٲ� op�� 1�� ���ؼ� ob�� �ֱ� (������ 01�� 1)

                    }
                    else if (strncmp(&token_table[i]->nixbpe, "00", 2) == 0) { // 00���� �����Ѵٸ�
                        strcpy(&ob, &inst_table[opcode]->op); // ob�� op�ֱ�
                        sprintf(&ob, "%02X", op2num); //���ڷ� �ٲ� op�� �ֱ�
                    }
                    int bin[4]; // ������ xbpe�� ������ ����ϱ� ���� �迭
                    for (int n = 0; n < 4; n++) { // x, b, p, e�� ���� �ֱ� ���� 4�� �ݺ�
                        if (strncmp(&token_table[i]->nixbpe + ((int)2 + (int)n), "0", 1) == 0) // ni�� ���� ���� 2�� ���ϰ� �������� ���� 0�� ���ؼ� ������
                            bin[n] = 0; // 0 �ֱ�
                        else // �ٸ���
                            bin[n] = 1; // 1 �ֱ�
                    }
                    sprintf(&ob[2], "%X", bin[0] * 8 + bin[1] * 4 + bin[2] * 2 + bin[3] * 1); // ni�� ����ִ� ob�� 2��°�� ����� ���� �ֱ�
                    if (strncmp(token_table[i]->operator, "+", 1) == 0) { // 4�����̸�
                        sprintf(&ob[3], "%05X", ta); // �ּҸ� 5�ڸ��� �ֱ�
                    }
                    else { // 4������ �ƴ϶��
                        if (strncmp(*token_table[i]->operand, "#", 1) == 0) { // operand�� #���� �����Ѵٸ�
                            sprintf(&ob[3], "%03X", strtoul(*token_table[i]->operand + 1, NULL, 16)); // #�ڿ� ���ڸ� ���ڷ� �ٲ㼭 3�ڸ��� �ֱ�
                        }
                        else if (strcmp(inst_table[search_opcode(token_table[i]->operator)]->format, "2") == 0) { //2�����̸�
                            if (strncmp(*token_table[i]->operand, "A", 1) == 0) { // operand�� A�� �����Ѵٸ�
                                strcpy(&ob[2], "0"); //0 �ֱ�
                            }
                            else if (strncmp(*token_table[i]->operand, "X", 1) == 0) { // operand�� X�� �����Ѵٸ�
                                strcpy(&ob[2], "1"); //1 �ֱ�
                            }
                            else if (strncmp(*token_table[i]->operand, "S", 1) == 0) { // operand�� S�� �����Ѵٸ�
                                strcpy(&ob[2], "4"); //4 �ֱ�
                            }
                            else if (strncmp(*token_table[i]->operand, "T", 1) == 0) { // operand�� T�� �����Ѵٸ�
                                strcpy(&ob[2], "5"); //5 �ֱ�
                            }
                            if (strncmp(*token_table[i]->operand + 2, "A", 1) == 0) { // ,�ڿ� �������Ͱ� A���
                                strcpy(&ob[3], "0"); //0 �ֱ�
                            }
                            else if (strncmp(*token_table[i]->operand + 2, "X", 1) == 0) { // ,�ڿ� �������Ͱ� X���
                                strcpy(&ob[3], "1"); //1 �ֱ�
                            }
                            else if (strncmp(*token_table[i]->operand + 2, "S", 1) == 0) { // ,�ڿ� �������Ͱ� S���
                                strcpy(&ob[3], "4"); //4 �ֱ�
                            }
                            else if (strncmp(*token_table[i]->operand + 2, "T", 1) == 0) { // ,�ڿ� �������Ͱ� T���
                                strcpy(&ob[3], "5"); //5 �ֱ�
                            }
                            else // ,�ڿ� �������Ͱ� ���ٸ�
                                strcpy(&ob[3], "0"); //0 �ֱ�
                        }
                        else { //�� �� �ƴ϶��
                            if (inst_table[opcode]->opn == 0) { // operand�� ���ٸ�
                                sprintf(&ob[3], "%03X", ta); // ta�� 3�ڸ��� �ֱ�
                            }
                            else { //operand�� �ִٸ�
                                if ((int)ta >= (int)pc) { // ta�� pc���� ũ�ų� ���ٸ�
                                    sprintf(&ob[3], "%03X", (int)ta - (int)pc); // ta���� pc�� ���� ���ڸ��� �ֱ�
                                }
                                if ((int)ta < (int)pc) { // ta�� pc���� �۴ٸ�
                                    sprintf(&ob[3], "%03X", (int)ta + (0x1000 - (int)pc)); // pc�� 1000���� �� ���� ta�� ���ؼ� ���ڸ��� �ֱ�
                                }
                            }
                        }
                    }
                }

            }          
            if (file_name != NULL) // ���ڰ� NULL���� �ƴ϶�� (���Ͽ� ����ؾ���)
            {
                FILE* file;
                if (i == 0) { // ������ ó�� ���ٸ�
                    file = fopen(file_name, "w"); //file_name�� ���� �������� ����
                }
                else { // ó�� ���°� �ƴ϶��
                    file = fopen(file_name, "a"); // file_name�� �̾�� �������� ����
                }
                if (strcmp(token_table[i]->operator, "START") == 0) { // operator�� START���
                    fprintf(file, "H%-6s%06X%06X\n", token_table[i]->label, sym_table[i].addr, PLen[0]); // Header �ۼ�
                    start = sym_table[i].addr; // ���� �ּҿ� START�� �ּҸ� �ֱ�
                }
                if (strcmp(token_table[i]->operator, "EXTDEF") == 0) { // operator�� EXTDEF���
                    char* ptr = strtok(*token_table[i]->operand, ", ");  // operand�� �ִ� �ҽ��ڵ带 ","�� ���� �������� ��ū �߶� ptr �����ͷ� ��ġ ����Ű��
                    char* sArr[10] = { NULL, }; // �ڸ� ��ū ���� �迭
                    int* addr = 0; // �ش� �ɺ��� �ּҸ� ���� ����
                    int j = 0; // operand�� ������ �̵��� ����
                    while (ptr != NULL) // �� �ٿ� �ִ� ��� operand�� ��ū���� �ڸ� ������ �ݺ�
                    {
                        sArr[j] = ptr; // �ڸ� ��ū�� j��° �迭�� �ֱ�
                        j++; // �迭 �̵� (���� ��ū���� �̵�)
                        ptr = strtok(NULL, ", "); // �ڸ� ��ū �������� ","�� ���� �������� ��ū �߶� ptr �����ͷ� ��ġ ����Ű��
                    }
                    fprintf(file, "D"); // DEF �ۼ�
                    for (int n = 0; n < j; n++) { // 0���� ���� ������ŭ
                        for (int k = 1; k < token_line; k++) { // �ɺ� ���̺� ������ �ݺ�
                            if (strcmp(sArr[n], &sym_table[k].symbol) == 0) // �ڸ� ��ū�� �ɺ��� ���ؼ� ���ٸ�
                            {
                                addr = sym_table[k].addr; // addr�� �ɺ� �ּ� �ֱ�
                            }
                        }
                        fprintf(file, "%-6s%06X", sArr[n], addr); // DEF �ۼ�
                    }
                    fprintf(file, "\n"); // �ٹٲ�
                }
                if (strcmp(token_table[i]->operator, "EXTREF") == 0) { // operator�� EXTREF���
                    fprintf(file, "R"); // REF �ۼ�
                    for (int n = 0; n < r3; n++) { // 0���� ���� ���� ��ŭ
                        fprintf(file, "%-6s", REF[r1][n]); // REF �ۼ�
                    }
                }
                if (strcmp(token_table[i]->operator, "START") != 0 && strcmp(token_table[i]->operator, "EXTDEF") != 0 && strcmp(token_table[i]->operator, "EXTREF") != 0) { // operator�� START, EXTDEF, EXTREF ��� �ƴ϶��
                    if (*token_table[i]->operand != NULL) { // operand�� �ִٸ�
                        n = 0; // n �ʱ�ȭ
                        while (REF[r2][n]) { // �ش� ������ REF�� ����Ǿ� �ִٸ�
                            if (strncmp(*token_table[i]->operand, REF[r2][n], strlen(REF[r2][n])) == 0) // operand�� REF�� REF���̸�ŭ ���ؼ� ���ٸ�
                            {
                                M[r2][mod] = (char*) malloc(sizeof(char) * 10); // M �迭 �����Ҵ�
                                if (strcmp(&ob, "000000") == 0) { // �� �ּҰ� 6�ڸ����
                                    MA[r2][mod] = (int)sym_table[i].addr; // MA�� �ش� �ɺ� �ּ� �ֱ�
                                    sprintf(M[r2][mod], "%02X+%s", 6, REF[r2][n]); // M�� 06+REF�ֱ�
                                }
                                else { // �� �ּҰ� 5�ڸ����
                                    MA[r2][mod] = (int)sym_table[i].addr + (int)1; // MA�� �ش� �ɺ� �ּ� +1 �ֱ�
                                    sprintf(M[r2][mod], "%02X+%s", 5, REF[r2][n]); // M�� 05+REF�ֱ�
                                }                                
                                mod++; // �������� �̵�
                                n++; // �������� �̵�
                            }
                            else // ���� �ʴٸ�
                                n++; // �������� �̵�
                        }
                    }
                }
                if (strncmp(&ob, "", 1) != 0) { //ob�� �ִٸ�
                    if (sym_table[i].addr == SAdd[r2][len]) { //�����ּҿ� �ش� ������ ���� �ּҰ� ���ٸ�
                        fprintf(file, "\nT%06X%02X", sym_table[i].addr, SLen[r2][len], r2, len); // TEXT �ۼ�
                        len++; // ���� �������� �̵�
                    }
                    fprintf(file, "%s", ob); // TEXT �ۼ�
                }
                if (strcmp(token_table[i]->operator, "CSECT") == 0) { // operator�� CSECT���
                    for (int n = 0; n < mod; n++) { //M�� ���� REF��ŭ �ݺ�
                        fprintf(file, "\nM%06X%-6s", MA[r2][n], M[r2][n]); //Modification �ۼ�
                    }
                    if (r2 == 1) { // 1��° �����϶�
                        memmove(M[r2][mod - 1]+2, "-BUFFER", 7); //������ Modification�� �����ؼ� +BUFEND�� -BUFFER ����
                        fprintf(file, "\nM%06X%-6s", MA[r2][mod-1], M[r2][mod-1]); //Modification �ۼ�
                    }
                    fprintf(file, "\nE"); // End �ۼ�
                    if (r2 == 0) { // 0��° �����϶�
                        fprintf(file, "%06X", start); // �����ּ� �ֱ�
                    }
                    r2++; //���� ��������
                    fprintf(file, "\n\nH%-6s%06X%06X\n", token_table[i]->label, sym_table[i].addr, PLen[r2]); // Header �ۼ�
                    mod = 0; // mod �ʱ�ȭ
                    len = 0; // len �ʱ�ȭ
                }  
                if (strcmp(token_table[i]->operator, "END") == 0) { // operator�� END���
                    for (int n = 0; n < mod; n++) { //M�� ���� REF��ŭ �ݺ�
                        fprintf(file, "\nM%06X%-6s", MA[r2][n], M[r2][n]); //Modification �ۼ�
                    }
                    fprintf(file, "\nE"); // End �ۼ�
                    mod = 0; // mod �ʱ�ȭ
                }
                fclose(file); // ���� �ݱ�
            }
            else // ���ڰ� NULL���̶�� (ȭ�鿡 ����ؾ���)
            {
            if (strcmp(token_table[i]->operator, "START") == 0) { // operator�� START���
                printf("H%-6s%06X%06X\n", token_table[i]->label, sym_table[i].addr, PLen[0]); // Header �ۼ�
                start = sym_table[i].addr; // ���� �ּҿ� START�� �ּҸ� �ֱ�
            }
            if (strcmp(token_table[i]->operator, "EXTDEF") == 0) { // operator�� EXTDEF���
                char* ptr = strtok(*token_table[i]->operand, ", "); // operand�� �ִ� �ҽ��ڵ带 ","�� ���� �������� ��ū �߶� ptr �����ͷ� ��ġ ����Ű��
                char* sArr[10] = { NULL, }; // �ڸ� ��ū ���� �迭
                int* addr = 0; // �ش� �ɺ��� �ּҸ� ���� ����
                int j = 0; // operand�� ������ �̵��� ����
                while (ptr != NULL) // �� �ٿ� �ִ� ��� operand�� ��ū���� �ڸ� ������ �ݺ�
                {
                    sArr[j] = ptr; // �ڸ� ��ū�� j��° �迭�� �ֱ�
                    j++; // �迭 �̵� (���� ��ū���� �̵�)
                    ptr = strtok(NULL, ", "); // �ڸ� ��ū �������� ","�� ���� �������� ��ū �߶� ptr �����ͷ� ��ġ ����Ű��
                }
                printf("D"); // DEF �ۼ�
                for (int n = 0; n < j; n++) { // 0���� ���� ������ŭ
                    for (int k = 1; k < token_line; k++) { // �ɺ� ���̺� ������ �ݺ�
                        if (strcmp(sArr[n], &sym_table[k].symbol) == 0) // �ڸ� ��ū�� �ɺ��� ���ؼ� ���ٸ�
                        {
                            addr = sym_table[k].addr; // addr�� �ɺ� �ּ� �ֱ�
                        }
                    }
                    printf("%-6s%06X", sArr[n], addr); // DEF �ۼ�
                }
                printf("\n"); // �ٹٲ�
            }
            if (strcmp(token_table[i]->operator, "EXTREF") == 0) { // operator�� EXTREF���
                printf("R"); // REF �ۼ�
                for (int n = 0; n < r3; n++) { // 0���� ���� ���� ��ŭ
                    printf("%-6s", REF[r1][n]); // REF �ۼ�
                }
            }
            if (strcmp(token_table[i]->operator, "START") != 0 && strcmp(token_table[i]->operator, "EXTDEF") != 0 && strcmp(token_table[i]->operator, "EXTREF") != 0) { // operator�� START, EXTDEF, EXTREF ��� �ƴ϶��
                if (*token_table[i]->operand != NULL) { // operand�� �ִٸ�
                    n = 0;
                    while (REF[r2][n]) { // �ش� ������ REF�� ����Ǿ� �ִٸ�
                        if (strncmp(*token_table[i]->operand, REF[r2][n], strlen(REF[r2][n])) == 0) // operand�� REF�� REF���̸�ŭ ���ؼ� ���ٸ�
                        {
                            M[r2][mod] = (char*)malloc(sizeof(char) * 10); // M �迭 �����Ҵ�
                            if (strcmp(&ob, "000000") == 0) { // �� �ּҰ� 6�ڸ����
                                MA[r2][mod] = (int)sym_table[i].addr; // MA�� �ش� �ɺ� �ּ� �ֱ�
                                sprintf(M[r2][mod], "%02X+%s", 6, REF[r2][n]); // M�� 06+REF�ֱ�
                            }
                            else { // �� �ּҰ� 5�ڸ����
                                MA[r2][mod] = (int)sym_table[i].addr + (int)1; // MA�� �ش� �ɺ� �ּ� +1 �ֱ�
                                sprintf(M[r2][mod], "%02X+%s", 5, REF[r2][n]); // M�� 05+REF�ֱ�
                            }
                            mod++; // �������� �̵�
                            n++; // �������� �̵�
                        }
                        else // ���� �ʴٸ�
                            n++; // �������� �̵�
                    }
                }
            }
            if (strncmp(&ob, "", 1) != 0) { //ob�� �ִٸ�
                if (sym_table[i].addr == SAdd[r2][len]) { //�����ּҿ� �ش� ������ ���� �ּҰ� ���ٸ�
                    printf("\nT%06X%02X", sym_table[i].addr, SLen[r2][len], r2, len); // TEXT �ۼ�
                    len++; // ���� �������� �̵�
                }
                printf("%s", ob); // TEXT �ۼ�
            }
            if (strcmp(token_table[i]->operator, "CSECT") == 0) { // operator�� CSECT���
                for (int n = 0; n < mod; n++) { //M�� ���� REF��ŭ �ݺ�
                    printf("\nM%06X%-6s", MA[r2][n], M[r2][n]); //Modification �ۼ�
                }
                if (r2 == 1) { // 1��° �����϶�
                    memmove(M[r2][mod - 1] + 2, "-BUFFER", 7); //������ Modification�� �����ؼ� +BUFEND�� -BUFFER ����
                    printf("\nM%06X%-6s", MA[r2][mod - 1], M[r2][mod - 1]); //Modification �ۼ�
                }
                printf("\nE"); // End �ۼ�
                if (r2 == 0) { // 0��° �����϶�
                    printf("%06X", start); // �����ּ� �ֱ�
                }
                r2++; //���� ��������
                printf("\n\nH%-6s%06X%06X\n", token_table[i]->label, sym_table[i].addr, PLen[r2]); // Header �ۼ�
                mod = 0; // mod �ʱ�ȭ
                len = 0; // len �ʱ�ȭ
            }
            if (strcmp(token_table[i]->operator, "END") == 0) { // operator�� END���
                for (int n = 0; n < mod; n++) { //M�� ���� REF��ŭ �ݺ�
                    printf("\nM%06X%-6s", MA[r2][n], M[r2][n]); //Modification �ۼ�
                }
                printf("\nE"); // End �ۼ�
                mod = 0; // mod �ʱ�ȭ
            }
            }
        }
    }
}
