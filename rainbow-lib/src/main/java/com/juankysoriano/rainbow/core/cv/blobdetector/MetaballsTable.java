package com.juankysoriano.rainbow.core.cv.blobdetector;

//==================================================
//class MetaballsTable
//==================================================
public class MetaballsTable {
    // Edge Cut Array
// ------------------------------
    public static int edgeCut[][] =
            {
                    {-1, -1, -1, -1, -1}, //0
                    {0, 3, -1, -1, -1}, //3
                    {0, 1, -1, -1, -1}, //1
                    {3, 1, -1, -1, -1}, //2
                    {1, 2, -1, -1, -1}, //0
                    {1, 2, 0, 3, -1}, //3
                    {0, 2, -1, -1, -1}, //1
                    {3, 2, -1, -1, -1}, //2
                    {3, 2, -1, -1, -1}, //2
                    {0, 2, -1, -1, -1}, //1
                    {1, 2, 0, 3, -1}, //3
                    {1, 2, -1, -1, -1}, //0
                    {3, 1, -1, -1, -1}, //2
                    {0, 1, -1, -1, -1}, //1
                    {0, 3, -1, -1, -1}, //3
                    {-1, -1, -1, -1, -1}  //0
            };

    // EdgeOffsetInfo Array
// ------------------------------
    public static int edgeOffsetInfo[][] =
            {
                    {0, 0, 0},
                    {1, 0, 1},
                    {0, 1, 0},
                    {0, 0, 1}

            };

    // EdgeToCompute Array
// ------------------------------
    public static int edgeToCompute[] = {0, 3, 1, 2, 0, 3, 1, 2, 2, 1, 3, 0, 2, 1, 3, 0};

    // neightborVoxel Array
// ------------------------------
// bit 0 : X+1
// bit 1 : X-1
// bit 2 : Y+1
// bit 3 : Y-1
    public static byte neightborVoxel[] = {0, 10, 9, 3, 5, 15, 12, 6, 6, 12, 12, 5, 3, 9, 10, 0};

    public static void computeNeighborTable() {
        int iEdge;
        int n;
        for (int i = 0; i < 16; i++) {
            neightborVoxel[i] = 0;

            n = 0;
            while ((iEdge = MetaballsTable.edgeCut[i][n++]) != -1) {
                switch (iEdge) {
                    case 0:
                        neightborVoxel[i] |= (1 << 3);
                        break;
                    case 1:
                        neightborVoxel[i] |= (1 << 0);
                        break;
                    case 2:
                        neightborVoxel[i] |= (1 << 2);
                        break;
                    case 3:
                        neightborVoxel[i] |= (1 << 1);
                        break;
                }
            }

        } // end for i

    }

}
