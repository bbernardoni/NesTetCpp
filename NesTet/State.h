#pragma once

struct State {
	int x;
	int y;
	int rotation;
	bool visited;
	
	State(int nX, int nY, int nRotation) {
		x = nX;
		y = nY;
		rotation = nRotation;
		visited = false;
	}
	State() {}
};
