#pragma once
#ifndef NAMESPACE_MYSERVERCLASS_H
#define NAMESPACE_MYSERVERCLASS_H

#include "SysThread.h"

class ClientThread : public SysThread {
private:
	char username[16] = {};
	SOCKET socket;
	vector <ClientThread*>* clientListRef;
	CRITICAL_SECTION* critSection;

public:
	ClientThread(SOCKET, vector <ClientThread*>*, CRITICAL_SECTION*);
	void run();
	int sendMSG2All(char[],char[]);
	int sendPrivateMessage(char[], char[]);
	int sendGroupMessage(char[], char[], char[]);
	int echoMSG(char[]);
	SOCKET getSocket();
};

//--------------------------------------------------
//Package
typedef struct {
	int type; //1byte
	int size; //4byte
	char from[17];
	char to[2048];
	char message[2048];
}Package;

#endif