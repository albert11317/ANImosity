//
// PacMan
// Another 1 day game (created in 5 hours).
//
// (C)2000
// Brian Postma
// b.postma@hetnet.nl
// http://www.homepages.hetnet.nl/~brianpostma
//

import java.awt.*;
import java.applet.Applet;

public class PacMan extends Applet implements Runnable {
	Dimension d;
	Font largefont = new Font("Helvetica", Font.BOLD, 24);
	Font smallfont = new Font("Helvetica", Font.BOLD, 14);

	FontMetrics fmsmall, fmlarge;
	Graphics goff;
	Image ii;
	Thread thethread;
	MediaTracker thetracker = null;
	Color dotcolor = new Color(192, 192, 0);
	int bigdotcolor = 192;
	int dbigdotcolor = -2;
	Color mazecolor;

	boolean ingame = false;
	boolean showtitle = true;
	boolean scared = false;
	boolean dying = false;

	final int screendelay = 120;
	final int blocksize = 24;
	final int nrofblocks = 15;
	final int scrsize = nrofblocks * blocksize;
	final int animdelay = 8;
	final int pacanimdelay = 2;
	final int ghostanimcount = 2;
	final int pacmananimcount = 4;
	final int maxghosts = 12;
	final int pacmanspeed = 6;

	int animcount = animdelay;
	int pacanimcount = pacanimdelay;
	int pacanimdir = 1;
	int count = screendelay;
	int ghostanimpos = 0;
	int pacmananimpos = 0;
	int nrofghosts = 6;
	int pacsleft, score;
	int deathcounter;
	int[] dx, dy;
	int[] ghostx, ghosty, ghostdx, ghostdy, ghostspeed;

	Image ghost1, ghost2, ghostscared1, ghostscared2;
	Image pacman1, pacman2up, pacman2left, pacman2right, pacman2down;
	Image pacman3up, pacman3down, pacman3left, pacman3right;
	Image pacman4up, pacman4down, pacman4left, pacman4right;

	int pacmanx, pacmany, pacmandx, pacmandy;
	int reqdx, reqdy, viewdx, viewdy;
	int scaredcount, scaredtime;
	final int maxscaredtime = 120;
	final int minscaredtime = 20;

	final short level1data[] = { 19, 26, 26, 22, 9, 12, 19, 26, 22, 9, 12, 19,
			26, 26, 22, 37, 11, 14, 17, 26, 26, 20, 15, 17, 26, 26, 20, 11, 14,
			37, 17, 26, 26, 20, 11, 6, 17, 26, 20, 3, 14, 17, 26, 26, 20, 21,
			3, 6, 25, 22, 5, 21, 7, 21, 5, 19, 28, 3, 6, 21, 21, 9, 8, 14, 21,
			13, 21, 5, 21, 13, 21, 11, 8, 12, 21, 25, 18, 26, 18, 24, 18, 28,
			5, 25, 18, 24, 18, 26, 18, 28, 6, 21, 7, 21, 7, 21, 11, 8, 14, 21,
			7, 21, 7, 21, 03, 4, 21, 5, 21, 5, 21, 11, 10, 14, 21, 5, 21, 5,
			21, 1, 12, 21, 13, 21, 13, 21, 11, 10, 14, 21, 13, 21, 13, 21, 9,
			19, 24, 26, 24, 26, 16, 26, 18, 26, 16, 26, 24, 26, 24, 22, 21, 3,
			2, 2, 6, 21, 15, 21, 15, 21, 3, 2, 2, 06, 21, 21, 9, 8, 8, 4, 17,
			26, 8, 26, 20, 1, 8, 8, 12, 21, 17, 26, 26, 22, 13, 21, 11, 2, 14,
			21, 13, 19, 26, 26, 20, 37, 11, 14, 17, 26, 24, 22, 13, 19, 24, 26,
			20, 11, 14, 37, 25, 26, 26, 28, 3, 6, 25, 26, 28, 3, 6, 25, 26, 26,
			28 };

	final int validspeeds[] = { 1, 2, 3, 4, 6, 6 };
	final int maxspeed = 6;

	int currentspeed = 3;
	short[] screendata;

	public String getAppletInfo() {
		return ("PacMan - by Brian Postma");
	}

	public void init() {
		short i;
		GetImages();

		screendata = new short[nrofblocks * nrofblocks];

		Graphics g;
		d = size();
		d.setSize(1280,720);
		this.setSize(1280,720);
		setBackground(Color.black);
		g = getGraphics();
		g.setFont(smallfont);
		fmsmall = g.getFontMetrics();
		g.setFont(largefont);
		fmlarge = g.getFontMetrics();
		ghostx = new int[maxghosts];
		ghostdx = new int[maxghosts];
		ghosty = new int[maxghosts];
		ghostdy = new int[maxghosts];
		ghostspeed = new int[maxghosts];
		dx = new int[4];
		dy = new int[4];
		GameInit();
	}

	public void GameInit() {
		pacsleft = 3;
		score = 0;
		scaredtime = maxscaredtime;
		LevelInit();
		nrofghosts = 6;
		currentspeed = 3;
		scaredtime = maxscaredtime;
	}

	public void LevelInit() {
		int i;
		for (i = 0; i < nrofblocks * nrofblocks; i++)
			screendata[i] = level1data[i];

		LevelContinue();
	}

	public void LevelContinue() {
		short i;
		int dx = 1;
		int random;

		for (i = 0; i < nrofghosts; i++) {
			ghosty[i] = 7 * blocksize;
			ghostx[i] = 7 * blocksize;
			ghostdy[i] = 0;
			ghostdx[i] = dx;
			dx = -dx;
			random = (int) (Math.random() * (currentspeed));
			if (random > (currentspeed - 1))
				random = currentspeed - 1;
			ghostspeed[i] = validspeeds[random];
		}
		screendata[7 * nrofblocks + 6] = 10;
		screendata[7 * nrofblocks + 8] = 10;
		pacmanx = 7 * blocksize;
		pacmany = 11 * blocksize;
		pacmandx = 0;
		pacmandy = 0;
		reqdx = 0;
		reqdy = 0;
		viewdx = -1;
		viewdy = 0;
		dying = false;
		scared = false;
	}

	public void GetImages() {
		thetracker = new MediaTracker(this);

		ghost1 = getImage(getDocumentBase(), "pacpix/Ghost1.gif");
		thetracker.addImage(ghost1, 0);
		ghost2 = getImage(getDocumentBase(), "pacpix/Ghost2.gif");
		thetracker.addImage(ghost2, 0);
		ghostscared1 = getImage(getDocumentBase(), "pacpix/GhostScared1.gif");
		thetracker.addImage(ghostscared1, 0);
		ghostscared2 = getImage(getDocumentBase(), "pacpix/GhostScared2.gif");
		thetracker.addImage(ghostscared2, 0);

		pacman1 = getImage(getDocumentBase(), "pacpix/PacMan1.gif");
		thetracker.addImage(pacman1, 0);
		pacman2up = getImage(getDocumentBase(), "pacpix/PacMan2up.gif");
		thetracker.addImage(pacman2up, 0);
		pacman3up = getImage(getDocumentBase(), "pacpix/PacMan3up.gif");
		thetracker.addImage(pacman3up, 0);
		pacman4up = getImage(getDocumentBase(), "pacpix/PacMan4up.gif");
		thetracker.addImage(pacman4up, 0);

		pacman2down = getImage(getDocumentBase(), "pacpix/PacMan2down.gif");
		thetracker.addImage(pacman2down, 0);
		pacman3down = getImage(getDocumentBase(), "pacpix/PacMan3down.gif");
		thetracker.addImage(pacman3down, 0);
		pacman4down = getImage(getDocumentBase(), "pacpix/PacMan4down.gif");
		thetracker.addImage(pacman4down, 0);

		pacman2left = getImage(getDocumentBase(), "pacpix/PacMan2left.gif");
		thetracker.addImage(pacman2left, 0);
		pacman3left = getImage(getDocumentBase(), "pacpix/PacMan3left.gif");
		thetracker.addImage(pacman3left, 0);
		pacman4left = getImage(getDocumentBase(), "pacpix/PacMan4left.gif");
		thetracker.addImage(pacman4left, 0);

		pacman2right = getImage(getDocumentBase(), "pacpix/PacMan2right.gif");
		thetracker.addImage(pacman2right, 0);
		pacman3right = getImage(getDocumentBase(), "pacpix/PacMan3right.gif");
		thetracker.addImage(pacman3right, 0);
		pacman4right = getImage(getDocumentBase(), "pacpix/PacMan4right.gif");
		thetracker.addImage(pacman4right, 0);

		try {
			thetracker.waitForAll();
		} catch (InterruptedException e) {
			return;
		}
	}

	public boolean keyDown(Event e, int key) {
		if (ingame) {
			if (key == Event.LEFT) {
				reqdx = -1;
				reqdy = 0;
			} else if (key == Event.RIGHT) {
				reqdx = 1;
				reqdy = 0;
			} else if (key == Event.UP) {
				reqdx = 0;
				reqdy = -1;
			} else if (key == Event.DOWN) {
				reqdx = 0;
				reqdy = 1;
			} else if (key == Event.ESCAPE) {
				ingame = false;
			}
		} else {
			if (key == 's' || key == 'S') {
				ingame = true;
				GameInit();
			}
		}
		return true;
	}

	public boolean keyUp(Event e, int key) {
		if (key == Event.LEFT || key == Event.RIGHT || key == Event.UP
				|| key == Event.DOWN) {
			reqdx = 0;
			reqdy = 0;
		}
		return true;
	}

	public void paint(Graphics g) {
		String s;
		Graphics gg;

		if (goff == null && d.width > 0 && d.height > 0) {
			ii = createImage(d.width, d.height);
			goff = ii.getGraphics();
		}
		if (goff == null || ii == null)
			return;

		goff.setColor(Color.black);
		goff.fillRect(0, 0, d.width, d.height);

		DrawMaze();
		DrawScore();
		DoAnim();
		if (ingame)
			PlayGame();
		else
			PlayDemo();

		g.drawImage(ii, 0, 0, this);
	}

	public void DoAnim() {
		animcount--;
		if (animcount <= 0) {
			animcount = animdelay;
			ghostanimpos++;
			if (ghostanimpos >= ghostanimcount)
				ghostanimpos = 0;
		}
		pacanimcount--;
		if (pacanimcount <= 0) {
			pacanimcount = pacanimdelay;
			pacmananimpos = pacmananimpos + pacanimdir;
			if (pacmananimpos == (pacmananimcount - 1) || pacmananimpos == 0)
				pacanimdir = -pacanimdir;
		}
	}

	public void PlayGame() {
		if (dying) {
			Death();
		} else {
			CheckScared();
			MovePacMan();
			DrawPacMan();
			MoveGhosts();
			CheckMaze();
		}
	}

	public void PlayDemo() {
		CheckScared();
		MoveGhosts();
		ShowIntroScreen();
	}

	public void Death() {
		int k;

		deathcounter--;
		k = (deathcounter & 15) / 4;
		switch (k) {
		case 0:
			goff.drawImage(pacman4up, pacmanx + 1, pacmany + 1, this);
			break;
		case 1:
			goff.drawImage(pacman4right, pacmanx + 1, pacmany + 1, this);
			break;
		case 2:
			goff.drawImage(pacman4down, pacmanx + 1, pacmany + 1, this);
			break;
		default:
			goff.drawImage(pacman4left, pacmanx + 1, pacmany + 1, this);
		}
		if (deathcounter == 0) {
			pacsleft--;
			if (pacsleft == 0)
				ingame = false;
			LevelContinue();
		}
	}

	public void MoveGhosts() {
		short i;
		int pos;
		int count;

		for (i = 0; i < nrofghosts; i++) {
			if (ghostx[i] % blocksize == 0 && ghosty[i] % blocksize == 0) {
				pos = ghostx[i] / blocksize + nrofblocks
						* (int) (ghosty[i] / blocksize);

				count = 0;
				if ((screendata[pos] & 1) == 0 && ghostdx[i] != 1) {
					dx[count] = -1;
					dy[count] = 0;
					count++;
				}
				if ((screendata[pos] & 2) == 0 && ghostdy[i] != 1) {
					dx[count] = 0;
					dy[count] = -1;
					count++;
				}
				if ((screendata[pos] & 4) == 0 && ghostdx[i] != -1) {
					dx[count] = 1;
					dy[count] = 0;
					count++;
				}
				if ((screendata[pos] & 8) == 0 && ghostdy[i] != -1) {
					dx[count] = 0;
					dy[count] = 1;
					count++;
				}
				if (count == 0) {
					if ((screendata[pos] & 15) == 15) {
						ghostdx[i] = 0;
						ghostdy[i] = 0;
					} else {
						ghostdx[i] = -ghostdx[i];
						ghostdy[i] = -ghostdy[i];
					}
				} else {
					count = (int) (Math.random() * count);
					if (count > 3)
						count = 3;
					ghostdx[i] = dx[count];
					ghostdy[i] = dy[count];
				}
			}
			ghostx[i] = ghostx[i] + (ghostdx[i] * ghostspeed[i]);
			ghosty[i] = ghosty[i] + (ghostdy[i] * ghostspeed[i]);
			DrawGhost(ghostx[i] + 1, ghosty[i] + 1);

			if (pacmanx > (ghostx[i] - 12) && pacmanx < (ghostx[i] + 12)
					&& pacmany > (ghosty[i] - 12) && pacmany < (ghosty[i] + 12)
					&& ingame) {
				if (scared) {
					score += 10;
					ghostx[i] = 7 * blocksize;
					ghosty[i] = 7 * blocksize;
				} else {
					dying = true;
					deathcounter = 64;
				}
			}
		}
	}

	public void DrawGhost(int x, int y) {
		if (ghostanimpos == 0 && !scared) {
			goff.drawImage(ghost1, x, y, this);
		} else if (ghostanimpos == 1 && !scared) {
			goff.drawImage(ghost2, x, y, this);
		} else if (ghostanimpos == 0 && scared) {
			goff.drawImage(ghostscared1, x, y, this);
		} else if (ghostanimpos == 1 && scared) {
			goff.drawImage(ghostscared2, x, y, this);
		}
	}

	public void MovePacMan() {
		int pos;
		short ch;

		if (reqdx == -pacmandx && reqdy == -pacmandy) {
			pacmandx = reqdx;
			pacmandy = reqdy;
			viewdx = pacmandx;
			viewdy = pacmandy;
		}
		if (pacmanx % blocksize == 0 && pacmany % blocksize == 0) {
			pos = pacmanx / blocksize + nrofblocks
					* (int) (pacmany / blocksize);
			ch = screendata[pos];
			if ((ch & 16) != 0) {
				screendata[pos] = (short) (ch & 15);
				score++;
			}
			if ((ch & 32) != 0) {
				scared = true;
				scaredcount = scaredtime;
				screendata[pos] = (short) (ch & 15);
				score += 5;
			}

			if (reqdx != 0 || reqdy != 0) {
				if (!((reqdx == -1 && reqdy == 0 && (ch & 1) != 0)
						|| (reqdx == 1 && reqdy == 0 && (ch & 4) != 0)
						|| (reqdx == 0 && reqdy == -1 && (ch & 2) != 0) || (reqdx == 0
						&& reqdy == 1 && (ch & 8) != 0))) {
					pacmandx = reqdx;
					pacmandy = reqdy;
					viewdx = pacmandx;
					viewdy = pacmandy;
				}
			}

			// Check for standstill
			if ((pacmandx == -1 && pacmandy == 0 && (ch & 1) != 0)
					|| (pacmandx == 1 && pacmandy == 0 && (ch & 4) != 0)
					|| (pacmandx == 0 && pacmandy == -1 && (ch & 2) != 0)
					|| (pacmandx == 0 && pacmandy == 1 && (ch & 8) != 0)) {
				pacmandx = 0;
				pacmandy = 0;
			}
		}
		pacmanx = pacmanx + pacmanspeed * pacmandx;
		pacmany = pacmany + pacmanspeed * pacmandy;
	}

	public void DrawPacMan() {
		if (viewdx == -1)
			DrawPacManLeft();
		else if (viewdx == 1)
			DrawPacManRight();
		else if (viewdy == -1)
			DrawPacManUp();
		else
			DrawPacManDown();
	}

	public void DrawPacManUp() {
		switch (pacmananimpos) {
		case 1:
			goff.drawImage(pacman2up, pacmanx + 1, pacmany + 1, this);
			break;
		case 2:
			goff.drawImage(pacman3up, pacmanx + 1, pacmany + 1, this);
			break;
		case 3:
			goff.drawImage(pacman4up, pacmanx + 1, pacmany + 1, this);
			break;
		default:
			goff.drawImage(pacman1, pacmanx + 1, pacmany + 1, this);
			break;
		}
	}

	public void DrawPacManDown() {
		switch (pacmananimpos) {
		case 1:
			goff.drawImage(pacman2down, pacmanx + 1, pacmany + 1, this);
			break;
		case 2:
			goff.drawImage(pacman3down, pacmanx + 1, pacmany + 1, this);
			break;
		case 3:
			goff.drawImage(pacman4down, pacmanx + 1, pacmany + 1, this);
			break;
		default:
			goff.drawImage(pacman1, pacmanx + 1, pacmany + 1, this);
			break;
		}
	}

	public void DrawPacManLeft() {
		switch (pacmananimpos) {
		case 1:
			goff.drawImage(pacman2left, pacmanx + 1, pacmany + 1, this);
			break;
		case 2:
			goff.drawImage(pacman3left, pacmanx + 1, pacmany + 1, this);
			break;
		case 3:
			goff.drawImage(pacman4left, pacmanx + 1, pacmany + 1, this);
			break;
		default:
			goff.drawImage(pacman1, pacmanx + 1, pacmany + 1, this);
			break;
		}
	}

	public void DrawPacManRight() {
		switch (pacmananimpos) {
		case 1:
			goff.drawImage(pacman2right, pacmanx + 1, pacmany + 1, this);
			break;
		case 2:
			goff.drawImage(pacman3right, pacmanx + 1, pacmany + 1, this);
			break;
		case 3:
			goff.drawImage(pacman4right, pacmanx + 1, pacmany + 1, this);
			break;
		default:
			goff.drawImage(pacman1, pacmanx + 1, pacmany + 1, this);
			break;
		}
	}

	public void DrawMaze() {
		short i = 0;
		int x, y;

		bigdotcolor = bigdotcolor + dbigdotcolor;
		if (bigdotcolor <= 64 || bigdotcolor >= 192)
			dbigdotcolor = -dbigdotcolor;

		for (y = 0; y < scrsize; y += blocksize) {
			for (x = 0; x < scrsize; x += blocksize) {
				goff.setColor(mazecolor);
				if ((screendata[i] & 1) != 0) {
					goff.drawLine(x, y, x, y + blocksize - 1);
				}
				if ((screendata[i] & 2) != 0) {
					goff.drawLine(x, y, x + blocksize - 1, y);
				}
				if ((screendata[i] & 4) != 0) {
					goff.drawLine(x + blocksize - 1, y, x + blocksize - 1, y
							+ blocksize - 1);
				}
				if ((screendata[i] & 8) != 0) {
					goff.drawLine(x, y + blocksize - 1, x + blocksize - 1, y
							+ blocksize - 1);
				}
				if ((screendata[i] & 16) != 0) {
					goff.setColor(dotcolor);
					goff.fillRect(x + 11, y + 11, 2, 2);
				}
				if ((screendata[i] & 32) != 0) {
					goff.setColor(new Color(224, 224 - bigdotcolor, bigdotcolor));
					goff.fillRect(x + 8, y + 8, 8, 8);
				}
				i++;
			}
		}
	}

	public void ShowIntroScreen() {
		String s;

		goff.setFont(largefont);

		goff.setColor(new Color(0, 32, 48));
		goff.fillRect(16, scrsize / 2 - 40, scrsize - 32, 80);
		goff.setColor(Color.white);
		goff.drawRect(16, scrsize / 2 - 40, scrsize - 32, 80);

		if (showtitle) {
			s = "Java PacMan";
			scared = false;

			goff.setColor(Color.white);
			goff.drawString(s, (scrsize - fmlarge.stringWidth(s)) / 2 + 2,
					scrsize / 2 - 20 + 2);
			goff.setColor(new Color(96, 128, 255));
			goff.drawString(s, (scrsize - fmlarge.stringWidth(s)) / 2,
					scrsize / 2 - 20);

			s = "(c)2000 by Brian Postma";
			goff.setFont(smallfont);
			goff.setColor(new Color(255, 160, 64));
			goff.drawString(s, (scrsize - fmsmall.stringWidth(s)) / 2,
					scrsize / 2 + 10);

			s = "b.postma@hetnet.nl";
			goff.setColor(new Color(255, 160, 64));
			goff.drawString(s, (scrsize - fmsmall.stringWidth(s)) / 2,
					scrsize / 2 + 30);
		} else {
			goff.setFont(smallfont);
			goff.setColor(new Color(96, 128, 255));
			s = "'S' to start game";
			goff.drawString(s, (scrsize - fmsmall.stringWidth(s)) / 2,
					scrsize / 2 - 10);
			goff.setColor(new Color(255, 160, 64));
			s = "Use cursor keys to move";
			goff.drawString(s, (scrsize - fmsmall.stringWidth(s)) / 2,
					scrsize / 2 + 20);
			scared = true;
		}
		count--;
		if (count <= 0) {
			count = screendelay;
			showtitle = !showtitle;
		}
	}

	public void DrawScore() {
		int i;
		String s;

		goff.setFont(smallfont);
		goff.setColor(new Color(96, 128, 255));
		s = "Score: " + score;
		goff.drawString(s, scrsize / 2 + 96, scrsize + 16);
		for (i = 0; i < pacsleft; i++) {
			goff.drawImage(pacman3left, i * 28 + 8, scrsize + 1, this);
		}
	}

	public void CheckScared() {
		scaredcount--;
		if (scaredcount <= 0)
			scared = false;

		if (scared && scaredcount >= 30)
			mazecolor = new Color(192, 32, 255);
		else
			mazecolor = new Color(32, 192, 255);

		if (scared) {
			screendata[7 * nrofblocks + 6] = 11;
			screendata[7 * nrofblocks + 8] = 14;
		} else {
			screendata[7 * nrofblocks + 6] = 10;
			screendata[7 * nrofblocks + 8] = 10;
		}
	}

	public void CheckMaze() {
		short i = 0;
		boolean finished = true;

		while (i < nrofblocks * nrofblocks && finished) {
			if ((screendata[i] & 48) != 0)
				finished = false;
			i++;
		}
		if (finished) {
			score += 50;
			DrawScore();
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
			}
			if (nrofghosts < maxghosts)
				nrofghosts++;
			if (currentspeed < maxspeed)
				currentspeed++;
			scaredtime = scaredtime - 20;
			if (scaredtime < minscaredtime)
				scaredtime = minscaredtime;
			LevelInit();
		}
	}

	public void run() {
		long starttime;
		Graphics g;

		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		g = getGraphics();

		while (true) {
			starttime = System.currentTimeMillis();
			try {
				paint(g);
				starttime += 40;
				Thread.sleep(Math.max(0, starttime - System.currentTimeMillis()));
			} catch (InterruptedException e) {
				break;
			}
		}
	}

	public void start() {
		if (thethread == null) {
			thethread = new Thread(this);
			thethread.start();
		}
	}

	public void stop() {
		if (thethread != null) {
			thethread.stop();
			thethread = null;
		}
	}
}
