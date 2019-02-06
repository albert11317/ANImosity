import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JPanel;

public class ANImosityJAR extends JPanel implements Runnable, KeyListener {
	private static final long serialVersionUID = 1L;
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
	boolean congrats = false;

	final int screendelay = 60;
	final Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
	final int nrofblocks = 15;
	final int scrsize = 900;
	final int blocksize = scrsize / nrofblocks;
	final int animdelay = 8;
	final int pacanimdelay = 2;
	final int ghostanimcount = 2;
	final int pacmananimcount = 4;
	final int maxghosts = 9999;
	final int pacmanspeed = 6;

	int animcount = animdelay;
	int pacanimcount = pacanimdelay;
	int pacanimdir = 1;
	int count = screendelay;
	int ghostanimpos = 0;
	int pacmananimpos = 0;
	int nrofghosts = 6;
	int pacsleft, score, levelnumber;
	int deathcounter;
	int[] dx, dy;
	int[] ghostx, ghosty, ghostdx, ghostdy, ghostspeed;
	int buddy, aniket;
	int congratstimer;

	Image ghost1, ghostscared1;
	Image pacman2up, pacman2down;
	Image AniketGraduation, Eric;

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

	// final int validspeeds[] = { 1, 2, 3, 4, 6, 6 };
	final int maxspeed = 9999;

	int currentspeed = 3;
	short[] screendata;

	public ANImosityJAR() {
	}

	public static void main(String[] args) {
		Frame f = new Frame();
		f.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				System.exit(0);
			};
		});
		ANImosityJAR alpha = new ANImosityJAR();
		alpha.setSize(900, 1080);
		f.add(alpha);
		f.pack();
		alpha.init();
		f.addKeyListener(alpha);
		f.setFocusable(true);
		f.requestFocus();
		f.setSize(900 + 20, 1080 + 25); // add 20, seems enough for the Frame
		f.setTitle("ANImosity");
		f.show();
		alpha.start();
	}

	public void init() {
		short i;
		this.GetImages();
		screendata = new short[nrofblocks * nrofblocks];
		Graphics g;
		d = size();
		d.setSize(900, 1080);
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
		levelnumber = 1;
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
			random = (int) (Math.random() * (currentspeed)) + 1;
			if (random > (currentspeed - 1))
				random = currentspeed - 1;
			ghostspeed[i] = random;
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
		ghost1 = Toolkit.getDefaultToolkit().getImage(
				super.getClass().getResource("images/Ani.gif"));
		thetracker.addImage(ghost1, 0);

		ghostscared1 = Toolkit.getDefaultToolkit().getImage(
				super.getClass().getResource("images/Navid.gif"));
		thetracker.addImage(ghostscared1, 0);

		pacman2up = Toolkit.getDefaultToolkit().getImage(
				super.getClass().getResource("images/AniketUP.gif"));
		thetracker.addImage(pacman2up, 0);

		pacman2down = Toolkit.getDefaultToolkit().getImage(
				super.getClass().getResource("images/AniketDOWN.gif"));
		thetracker.addImage(pacman2down, 0);

		AniketGraduation = Toolkit.getDefaultToolkit().getImage(
				super.getClass().getResource("images/Aniket.gif"));
		thetracker.addImage(AniketGraduation, 0);

		Eric = Toolkit.getDefaultToolkit().getImage(
				super.getClass().getResource("images/Eric.gif"));
		thetracker.addImage(Eric, 0);

		try {
			thetracker.waitForAll();
		} catch (InterruptedException e) {
			return;
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		if (ingame) {
			if (key == KeyEvent.VK_LEFT) {
				reqdx = -1;
				reqdy = 0;
			} else if (key == KeyEvent.VK_RIGHT) {
				reqdx = 1;
				reqdy = 0;
			} else if (key == KeyEvent.VK_UP) {
				reqdx = 0;
				reqdy = -1;
			} else if (key == KeyEvent.VK_DOWN) {
				reqdx = 0;
				reqdy = 1;
			} else if (key == KeyEvent.VK_ESCAPE) {
				ingame = false;
			}
		} else {// Start Menu
			if (key == KeyEvent.VK_ESCAPE) {
				aniket = 0;
				buddy = 0;
			}else if (key == 'a' || key == 'A') {
				aniket++;
			} else if (key == 'n' || key == 'N') {
				aniket++;
			} else if (key == 'i' || key == 'I') {
				aniket++;
			} else if (key == 'k' || key == 'K') {
				aniket++;
			} else if (key == 'e' || key == 'E') {
				aniket++;
			} else if (key == 't' || key == 'T') {
				aniket++;
			} else if (key == 'b' || key == 'B') {
				buddy++;
			} else if (key == 'u' || key == 'U') {
				buddy++;
			} else if (key == 'd' || key == 'D') {
				buddy++;
			} else if (key == 'y' || key == 'Y') {
				buddy++;
			}
			if (aniket == 6) {
				aniket = 0;
				buddy = 0;
				ingame = true;
				GameInit();
			}
			if (buddy == 5) {
				aniket = 0;
				buddy = 0;
				ingame = true;
				congrats = true;
				GameInit();
			}
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		int key = e.getKeyCode();
		if (key == Event.LEFT || key == Event.RIGHT || key == Event.UP
				|| key == Event.DOWN) {
			reqdx = 0;
			reqdy = 0;
		}
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
		if (congrats) {
			if (congratstimer >= 0 && congratstimer <= 25) {
				goff.drawImage(AniketGraduation, 310, 410, this);
				goff.drawImage(Eric, 480, 410, this);
				goff.setFont(largefont);
				goff.setColor(Color.WHITE);
				goff.drawString("you are playing BUDDY mode", 310, 410);
				congratstimer++;
			} else if (congratstimer >= 25 && congratstimer <= 50) {
				goff.drawImage(AniketGraduation, 0, 0, this);
				goff.drawImage(Eric, 700, 800, this);
				congratstimer++;
			} else if (congratstimer >= 50 && congratstimer <= 75) {
				goff.drawImage(AniketGraduation, 700, 0, this);
				goff.drawImage(Eric, 0, 800, this);
				congratstimer++;
			} else if (congratstimer >= 75 && congratstimer <= 100) {
				goff.drawImage(AniketGraduation, 700, 800, this);
				goff.drawImage(Eric, 0, 0, this);
				congratstimer++;
			} else if (congratstimer >= 100 && congratstimer <= 125) {
				goff.drawImage(AniketGraduation, 0, 800, this);
				goff.drawImage(Eric, 700, 0, this);
				congratstimer++;
			} else if (congratstimer >= 125 && congratstimer <= 150) {
				goff.drawImage(AniketGraduation, 380, 410, this);
				goff.drawImage(Eric, 310, 410, this);
				congratstimer++;
			} else if (congratstimer == 151) {
				congratstimer = 0;
				goff.setFont(largefont);
				goff.setColor(Color.WHITE);
				goff.drawString("YOU ARE PLAYING BUDDY MODE", 310, 410);
			}
		}
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
			goff.drawImage(pacman2up, pacmanx + 7, pacmany + 2, this);
			break;
		case 1:
			goff.drawImage(pacman2down, pacmanx + 7, pacmany + 2, this);
			break;
		case 2:
			goff.drawImage(pacman2up, pacmanx + 7, pacmany + 2, this);
			break;
		default:
			goff.drawImage(pacman2down, pacmanx + 7, pacmany + 2, this);
		}
		if (deathcounter == 0) {
			pacsleft--;
			if (pacsleft == 0) {
				ingame = false;
			}
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
				int tempscreendata;
				tempscreendata = 0;
				try {
					tempscreendata = screendata[pos];
				} catch (java.lang.ArrayIndexOutOfBoundsException e) {
					tempscreendata = 26;
				}
				if ((tempscreendata & 1) == 0 && ghostdx[i] != 1) {// screendata[pos]
					dx[count] = -1;
					dy[count] = 0;
					count++;
				}
				if ((tempscreendata & 2) == 0 && ghostdy[i] != 1) {
					dx[count] = 0;
					dy[count] = -1;
					count++;
				}
				if ((tempscreendata & 4) == 0 && ghostdx[i] != -1) {
					dx[count] = 1;
					dy[count] = 0;
					count++;
				}
				if ((tempscreendata & 8) == 0 && ghostdy[i] != -1) {
					dx[count] = 0;
					dy[count] = 1;
					count++;
				}
				if (count == 0) {
					if ((tempscreendata & 15) == 15) {
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
		if (!scared) {
			goff.drawImage(ghost1, x + 5, y, this);
		} else if (scared) {
			goff.drawImage(ghostscared1, x + 5, y, this);
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
				score += (int) (Math.random() * (1000)) + 34539;
			}
			if ((ch & 32) != 0) {
				scared = true;
				scaredcount = scaredtime;
				screendata[pos] = (short) (ch & 15);
				score += (int) (Math.random() * (2000)) + 99389;
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
		goff.drawImage(pacman2up, pacmanx + 7, pacmany + 2, this);
	}

	public void DrawPacManDown() {
		goff.drawImage(pacman2up, pacmanx + 7, pacmany + 2, this);
	}

	public void DrawPacManLeft() {
		goff.drawImage(pacman2up, pacmanx + 7, pacmany + 2, this);
	}

	public void DrawPacManRight() {
		goff.drawImage(pacman2up, pacmanx + 7, pacmany + 2, this);
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
				int tempscreendata = screendata[i];
				if ((tempscreendata & 1) != 0) {
					goff.drawLine(x, y, x, y + blocksize - 1);
				}
				if ((tempscreendata & 2) != 0) {
					goff.drawLine(x, y, x + blocksize - 1, y);
				}
				if ((tempscreendata & 4) != 0) {
					goff.drawLine(x + blocksize - 1, y, x + blocksize - 1, y
							+ blocksize - 1);
				}
				if ((tempscreendata & 8) != 0) {
					goff.drawLine(x, y + blocksize - 1, x + blocksize - 1, y
							+ blocksize - 1);
				}
				if ((tempscreendata & 16) != 0) {
					goff.setColor(dotcolor);
					goff.fillRect(x + 25, y + 25, 10, 10);
				}
				if ((tempscreendata & 32) != 0) {
					goff.setColor(new Color(224, 224 - bigdotcolor, bigdotcolor));
					goff.fillRect(x + 20, y + 20, 20, 20);
				}
				i++;
			}
		}
	}

	public void ShowIntroScreen() {
		String s;

		goff.setFont(largefont);

		goff.setColor(new Color(0, 32, 48));
		goff.fillRect(310, 410, 275, 80);
		goff.setColor(Color.white);
		goff.drawRect(310, 410, 275, 80);

		if (showtitle) {
			s = "ANImosity";
			scared = false;

			goff.setColor(Color.white);
			goff.drawString(s, (900 - fmlarge.stringWidth(s)) / 2 + 2,
					900 / 2 - 20 + 2);
			goff.setColor(new Color(96, 128, 255));
			goff.drawString(s, (900 - fmlarge.stringWidth(s)) / 2, 900 / 2 - 20);

			s = "by Berty Bert";
			goff.setFont(smallfont);
			goff.setColor(new Color(255, 160, 64));
			goff.drawString(s, (900 - fmsmall.stringWidth(s)) / 2, 900 / 2 + 10);

			s = "Credits to Ani, Ani, and Navid";
			goff.setColor(new Color(255, 160, 64));
			goff.drawString(s, (900 - fmsmall.stringWidth(s)) / 2, 900 / 2 + 30);
		} else {
			goff.setFont(smallfont);
			goff.setColor(new Color(96, 128, 255));

			s = "Type 'ANIKET' to start game";
			goff.drawString(s, (900 - fmsmall.stringWidth(s)) / 2, 900 / 2 - 10);
			goff.setColor(new Color(255, 160, 64));

			s = "Use cursor keys to move";
			goff.drawString(s, (900 - fmsmall.stringWidth(s)) / 2, 900 / 2 + 20);
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
		String l;
		goff.setFont(smallfont);
		goff.setColor(new Color(96, 128, 255));
		s = "Score: " + score;
		l = "Level: " + levelnumber;
		goff.drawString(s, 700, 940);
		goff.drawString(l, 702, 955);
		for (i = 0; i < pacsleft; i++) {
			goff.drawImage(pacman2down, i * 55 + 100, 920, this);
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
			score += (int) (Math.random() * (1000000)) + 1908083;
			DrawScore();
			levelnumber++;
			if (congrats) {
				levelnumber++;
			}
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
			}
			if (nrofghosts < maxghosts)
				nrofghosts += 2;
			if (currentspeed < maxspeed)
				currentspeed += 5;
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
