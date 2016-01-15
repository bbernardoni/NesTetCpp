#pragma once

#include "Emu.h"
#include <Windows.h>
#include <Windowsx.h>
#include <process.h>

//#define AI_WITH_GUI

class GUI
{
public:
	GUI(bool mem[10][20], int curPiece, int nextPiece, int rand, int totalFrames);
	GUI(Emu *emu);
	~GUI();
	
	void createWindowThread();
	static void windowThread(void *_pThis);

	bool initWindow();
	static LRESULT CALLBACK WndProc(HWND hwnd, UINT msg, WPARAM wParam, LPARAM lParam);
	
	static void paint(HDC hdc, RECT* prc);
	static void update(RECT* prc);
	static void mouseClick(int x, int y);
	static void mouseMove(int x, int y);
	static void keyTyped(unsigned int key);
private:
	static Emu *emu;
	HANDLE m_hThreadHandle;
	
	static HBITMAP tile;
	static HBITMAP next;
	static HWND window;
};