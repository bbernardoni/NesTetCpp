#include "GUI.h"

Emu *GUI::emu = NULL;
HWND GUI::window = NULL;
HBITMAP GUI::tile = NULL;
HBITMAP GUI::next = NULL;

GUI::GUI(bool mem[10][20], int curPiece, int nextPiece, int rand, int totalFrames)
{
	emu = new Emu(mem, curPiece, nextPiece, rand, totalFrames);/*
	emu->placePiece(0,8,1);
	emu->incWaitFrames();
	emu->incWaitFrames();
	emu->incWaitFrames();
	emu->incWaitFrames();
	emu->placePiece(3,8,2);
	emu->placePiece(5,7,3);
	emu->placePiece(8,7,3);
	emu->placePiece(0,6,1);
	emu->incWaitFrames();
	emu->incWaitFrames();
	emu->placePiece(6,6,1);
	emu->placePiece(2,5,0);
	emu->placePiece(7,5,3);
	emu->placePiece(3,5,1);
	emu->placePiece(9,7,0);*/
    createWindowThread();
}
    
GUI::GUI(Emu *emu)
{
	this->emu = emu;
    createWindowThread();
}

GUI::~GUI()
{
	if(window > 0)
		DestroyWindow(window);
	if(m_hThreadHandle > 0)
		TerminateThread(m_hThreadHandle, 0);
}

void GUI::createWindowThread()
{
	m_hThreadHandle = 0;
	while (m_hThreadHandle<=0)
	{
		m_hThreadHandle = (HANDLE)(_beginthread(windowThread, 0,reinterpret_cast<void *>(this)));
		if (m_hThreadHandle<=0)
			Sleep(500);
	}
}

void GUI::windowThread(void *_pThis)
{
	GUI *pThis = reinterpret_cast<GUI *>(_pThis);
	pThis->initWindow();
	_endthread();
}

bool GUI::initWindow()
{
    WNDCLASSEX wndclass = { 
			sizeof(WNDCLASSEX),	// size of struct
			0,					// style
			WndProc,			// windows procedure
			0, 0,				// extra bytes, not relevant
			GetModuleHandle(0),	// window procedure instance handle
			LoadIcon(0,IDI_APPLICATION),		// icon
			LoadCursor(0,IDC_ARROW),			// cursor
			HBRUSH(GetStockObject(BLACK_BRUSH)),// window background color
			0,									// menu resource
			"Nes Tetris",						// class name
			LoadIcon(0,IDI_APPLICATION)			// small icon
			};

	if(!RegisterClassEx(&wndclass))
	{
		MessageBox(NULL, "Window Registration Failed!", "Error!", MB_ICONEXCLAMATION | MB_OK);
		return false;
	}

	window = CreateWindowEx(
			0,					// extended window style
			"Nes Tetris",		// class name
			"Tetris GUI",		// window title
			WS_OVERLAPPEDWINDOW,// window style
			CW_USEDEFAULT, CW_USEDEFAULT,// x and y position
			500, 678,			// width and height
			0,					// parent window
			0,					// menu handle
			GetModuleHandle(0),	// module instance handle
			0					// window params
			);

	if(!window)
	{
		MessageBox(NULL, "Window Creation Failed!", "Error!", MB_ICONEXCLAMATION | MB_OK);
		return false;
	}

	ShowWindow(window, SW_SHOWDEFAULT);
	UpdateWindow(window);

	MSG msg;
	while(GetMessage(&msg, NULL, 0, 0) > 0)
	{
		TranslateMessage(&msg);
		DispatchMessage(&msg);
	}
	return true;
}


LRESULT CALLBACK GUI::WndProc(HWND hwnd, UINT msg, WPARAM wParam, LPARAM lParam)
{
	switch(msg)
	{
	case WM_CREATE:
		tile = (HBITMAP) LoadImage(NULL, "img\\tile.bmp", IMAGE_BITMAP, 0, 0, LR_LOADFROMFILE);
		if(tile == NULL)
			MessageBox(hwnd, "Could not load IDB_TILE!", "Error", MB_OK | MB_ICONEXCLAMATION);
		next = (HBITMAP) LoadImage(NULL, "img\\next.bmp", IMAGE_BITMAP, 0, 0, LR_LOADFROMFILE);
		if(next == NULL)
			MessageBox(hwnd, "Could not load IDB_NEXT!", "Error", MB_OK | MB_ICONEXCLAMATION);
		if(SetTimer(hwnd, 1, 20, NULL) == 0)
			MessageBox(hwnd, "Could not SetTimer()!", "Error", MB_OK | MB_ICONEXCLAMATION);
		break;
	case WM_PAINT:
		{
			RECT rcClient;
			PAINTSTRUCT ps;
			HDC hdc = BeginPaint(hwnd, &ps);

			GetClientRect(hwnd, &rcClient);
			paint(hdc, &rcClient);

			EndPaint(hwnd, &ps);
		}break;
	case WM_TIMER:
		{
			RECT rcClient;
			HDC hdc = GetDC(hwnd);

			GetClientRect(hwnd, &rcClient);

			update(&rcClient);
			paint(hdc, &rcClient);

			ReleaseDC(hwnd, hdc);
		}break;
	case WM_LBUTTONDOWN:
		mouseClick(GET_X_LPARAM(lParam), GET_Y_LPARAM(lParam));
		break;
	case WM_MOUSEMOVE:
		mouseMove(GET_X_LPARAM(lParam), GET_Y_LPARAM(lParam));
		break;
	case WM_KEYDOWN:
		if(wParam == VK_ESCAPE)
			DestroyWindow(hwnd);
		else
			keyTyped(wParam);
		break;
	case WM_CLOSE:
		DestroyWindow(hwnd);
		break;
	case WM_DESTROY:
		KillTimer(hwnd, 1);
		DeleteObject(tile);
		DeleteObject(next);

		PostQuitMessage(0);
		break;
	default:
		return DefWindowProc(hwnd, msg, wParam, lParam);
	}
	return 0;
}

void GUI::paint(HDC hdc, RECT* prc)
{
	// set up buffers
	HDC hdcBuffer = CreateCompatibleDC(hdc);
	HBITMAP hbmBuffer = CreateCompatibleBitmap(hdc, prc->right, prc->bottom);
	HBITMAP hbmOldBuffer = (HBITMAP)SelectObject(hdcBuffer, hbmBuffer);
	HDC hdcMem = CreateCompatibleDC(hdc);
	HBITMAP hbmOld = (HBITMAP)SelectObject(hdcMem, next);

	// actually drawing
#ifndef AI_WITH_GUI
	if(emu->curPieceClear()){
        for(int i=0; i<4; i++){
			BitBlt(hdcBuffer, emu->getCurPieceX(i)*32, emu->getCurPieceY(i)*32, 32, 32, hdcMem, 0, 0, SRCCOPY);
        }
    }
    for(int i=0; i<4; i++){
		BitBlt(hdcBuffer, 400+emu->getNextPieceX(i)*32, 300+emu->getNextPieceY(i)*32, 32, 32, hdcMem, 0, 0, SRCCOPY);
    }
#endif

	SelectObject(hdcMem, tile);
    for(int x=0; x<10; x++){
        for(int y=0; y<20; y++){
            if(emu->isCellFilled(x,y)){
				BitBlt(hdcBuffer, x*32, y*32, 32, 32, hdcMem, 0, 0, SRCCOPY);
            }
        }
    }
	
#ifndef AI_WITH_GUI
	char szText[100];
	SetBkMode(hdcBuffer, TRANSPARENT);
	SetTextColor(hdcBuffer, RGB(0xFF,0xFF,0xFF));
	sprintf_s(szText, "px: %d", emu->px/32);
	TextOut(hdcBuffer, 340, 20, szText, strlen(szText));
	sprintf_s(szText, "py: %d", emu->py/32);
	TextOut(hdcBuffer, 340, 40, szText, strlen(szText));
	sprintf_s(szText, "Rotation: %d", emu->hist.back().rotation);
	TextOut(hdcBuffer, 340, 60, szText, strlen(szText));
	sprintf_s(szText, "Wait Frames: %d", emu->hist.back().waitFrames);
	TextOut(hdcBuffer, 340, 80, szText, strlen(szText));
	sprintf_s(szText, "Rand: 0x%x", emu->hist.back().rand);
	TextOut(hdcBuffer, 340, 100, szText, strlen(szText));
	sprintf_s(szText, "Total Frames: 0x%x", emu->hist.back().totalFrames);
	TextOut(hdcBuffer, 340, 120, szText, strlen(szText));
	sprintf_s(szText, "Lines Left: %d", (25-emu->linesCleared));
	TextOut(hdcBuffer, 340, 140, szText, strlen(szText));
#endif

	//draw buffer
	BitBlt(hdc, 0, 0, prc->right, prc->bottom, hdcBuffer, 0, 0, SRCCOPY);

	//clean up
	SelectObject(hdcMem, hbmOld);
	DeleteDC(hdcMem);
	SelectObject(hdcBuffer, hbmOldBuffer);
	DeleteDC(hdcBuffer);
	DeleteObject(hbmBuffer);
}

void GUI::update(RECT* prc)
{
	// nothing for now
}

void GUI::mouseClick(int x, int y)
{
	mouseMove(x, y);
	if(emu->curPieceClear()){
    	emu->placePiece();
    }
}

void GUI::mouseMove(int x, int y)
{
	emu->px = (x/32)*32;
	emu->py = (y/32)*32;
}

void GUI::keyTyped(unsigned int key)
{
	switch(key){
	case VK_UP:
	case 'W':
		emu->incWaitFrames();
		break;
	case 'E':
		emu->incCurWaitFrames();
		break;
	case VK_DOWN:
	case 'S':
		emu->decWaitFrames();
		break;
	case VK_RIGHT:
	case 'D':
		emu->rotClockwise();
		break;
	case VK_LEFT:
	case 'A':
		emu->rotCounterClockwise();
		break;
	case VK_BACK:
		emu->undoPlace();
		break;
	case VK_RETURN:
		emu->printGame();
		break;
	}
}