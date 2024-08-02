package arch.sm213.machine.student;

import machine.AbstractMainMemory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MainMemoryTest {
    MainMemory memory;
    byte b0;
    byte b1;
    byte b2;
    byte b3;
    byte b4;
    byte bf;

    @BeforeEach
    public void runBefore() {
        // create a new Main memory for our test class
        memory = new MainMemory(5);
        b0 = 0x00;
        b1 = 0x01;
        b2 = 0x7f;
        b3 = (byte) 0x80;
        b4 = (byte) 0xfe;
        bf = (byte) 0xff;
    }

    @Test
    public void testConstructor() {
        assertEquals(5, memory.length());
    }
    @Test
    public void testAligned() {
        // first four: when address is aligned with length, which means it can be divided with no remainder
        assertTrue(memory.isAccessAligned(4, 2));
        assertTrue(memory.isAccessAligned(4, 1));
        assertTrue(memory.isAccessAligned(4, 4));
        assertTrue(memory.isAccessAligned(2, 2));
        // this one is when the remainder equals to 1, and they are not aligned
        assertFalse(memory.isAccessAligned(3, 2));
        // this one is when address cannot be divided by length at all, the remainder is address itself,
        // they are not aligned
        assertFalse(memory.isAccessAligned(2, 4));
    }

    @Test
    public void testToInteger() {
        // test positive value, when first byte start with 0~7 in Hex ; or start with 0 in binary
        assertEquals(25133310, memory.bytesToInteger(b1,b2,b3,b4));
        // test negative value, when first byte start with 8~f in Hex ; or start with 1 in binary
        assertEquals(-25165441, memory.bytesToInteger(b4,b3,b1,b2));
        //test when all byte are f in Hex ; or 1 in binary
        assertEquals(-1, memory.bytesToInteger(bf,bf,bf,bf));
        // test when all byte are 0 in Hex ; or 0 in binary
        assertEquals(0, memory.bytesToInteger(b0,b0,b0,b0));
        // test when output is 1
        assertEquals(1, memory.bytesToInteger(b0,b0,b0,b1));
        // test when first byte is 0xff, another case similar with case1
        assertEquals(-98177, memory.bytesToInteger(bf,b4,b3,b2));
        // test when first byte is 0x7f, another case similar with case2
        assertEquals(2147352960, memory.bytesToInteger(b2,b4,b1,b3));
        // test boundary, when input is 0x7fffffff, the largest number (if plus one it will be negative)
        assertEquals(2147483647, memory.bytesToInteger(b2,bf,bf,bf));
        // test boundary, when input is 0x80000000, the smallest number (if minus one it will be positive)
        assertEquals(-2147483648, memory.bytesToInteger(b3,b0,b0,b0));
    }

    @Test
    public void testToBytes() {
        // test if it is Big endian or Small endian.
        assertArrayEquals(new byte[]{b0,b0,bf,bf}, memory.integerToBytes(65535));
        assertArrayEquals(new byte[]{bf,bf,b0,b0}, memory.integerToBytes(-65536));

        // all cases below is the opposite test with the previous test (testToInteger)
        // test positive value, when first byte start with 0~7 in Hex ; or start with 0 in binary
        assertArrayEquals(new byte[]{b1,b2,b3,b4}, memory.integerToBytes(25133310));
        // test negative value, when first byte start with 8~f in Hex ; or start with 1 in binary
        assertArrayEquals(new byte[]{b4,b3,b1,b2}, memory.integerToBytes(-25165441));
        //test when all byte are f in Hex ; or 1 in binary
        assertArrayEquals(new byte[]{bf,bf,bf,bf}, memory.integerToBytes(-1));
        // test when all byte are 0 in Hex ; or 0 in binary
        assertArrayEquals(new byte[]{b0,b0,b0,b0}, memory.integerToBytes(0));
        // test when output is 1
        assertArrayEquals(new byte[]{b0,b0,b0,b1}, memory.integerToBytes(1));
        // test when first byte is 0xff, another case similar with case1
        assertArrayEquals(new byte[]{bf,b4,b3,b2}, memory.integerToBytes(-98177));
        // test when first byte is 0x7f, another case similar with case2
        assertArrayEquals(new byte[]{b2,b4,b1,b3}, memory.integerToBytes(2147352960));
        // test boundary, when input is 0x7fffffff, the largest number (if plus one it will be negative)
        assertArrayEquals(new byte[]{b2,bf,bf,bf}, memory.integerToBytes(2147483647));
        // test boundary, when input is 0x80000000, the smallest number (if minus one it will be positive)
        assertArrayEquals(new byte[]{b3,b0,b0,b0}, memory.integerToBytes(-2147483648));
    }

    @Test
    public void testSetGetOnce() {
        // test at first memory is all 0
        try {
            assertArrayEquals(new byte[]{0,0,0,0,0}, memory.get(0, 5));
            //pass
        } catch (AbstractMainMemory.InvalidAddressException e) {
            fail("should not catch this exception");
        }
        // test put first 4 index with bytes
        try {
            memory.set(0, new byte[]{b1,b2,b3,b4});
            //pass
        } catch (AbstractMainMemory.InvalidAddressException e) {
            fail("should not catch this exception");
        }
        // test getter of first 4 index, if it is the same byte array we put
        try {
            assertArrayEquals(new byte[]{b1, b2, b3, b4}, memory.get(0, 4));
            //pass
        } catch (AbstractMainMemory.InvalidAddressException e) {
            fail("should not catch this exception");
        }

        // test put 1 byte to memory that doesn't cover previous memory
        try {
            memory.set(4, new byte[]{b0});
            //pass
        } catch (AbstractMainMemory.InvalidAddressException e) {
            fail("should not catch this exception");
        }
        // test getter of index 4, if it is the same byte array we put
        try {
            assertArrayEquals(new byte[]{b0}, memory.get(4, 1));
            //pass
        } catch (AbstractMainMemory.InvalidAddressException e) {
            fail("should not catch this exception");
        }

        // view the whole memory
        try {
            assertArrayEquals(new byte[]{b1,b2,b3,b4,b0}, memory.get(0, 5));
            //pass
        } catch (AbstractMainMemory.InvalidAddressException e) {
            fail("should not catch this exception");
        }
    }

    @Test
    public void testSetGetCovered() {
        // test put 3 byte to the first 3 index of memory
        try {
            memory.set(0, new byte[]{b1,b2,b3});
            //pass
        } catch (AbstractMainMemory.InvalidAddressException e) {
            fail("should not catch this exception");
        }
        // test getter of first 3 index, if it is the same byte array we put
        try {
            assertArrayEquals(new byte[]{b1,b2,b3}, memory.get(0, 3));
            //pass
        } catch (AbstractMainMemory.InvalidAddressException e) {
            fail("should not catch this exception");
        }

        // put different 3 byte to the first 3 index of memory, cover the last set method
        try {
            memory.set(0, new byte[]{b4,b0,bf});
            //pass
        } catch (AbstractMainMemory.InvalidAddressException e) {
            fail("should not catch this exception");
        }
        // test getter of first 3 index, see if it has been covered
        try {
            assertArrayEquals(new byte[]{b4,b0,bf}, memory.get(0, 3));
            //pass
        } catch (AbstractMainMemory.InvalidAddressException e) {
            fail("should not catch this exception");
        }
    }

    @Test
    public void testSetGetInvalid() {
        // put 6 bytes into memory start from index 0, which exceed the byte capacity of memory
        try {
            memory.set(0, new byte[]{b1,b2,b3,b1,b2,b3});
            fail("should not reach here");
        } catch (AbstractMainMemory.InvalidAddressException e) {
            // pass
        }

        // put 1 byte into memory start from index 9, which exceed the byte capacity of memory
        try {
            memory.set(9, new byte[]{b1});
            fail("should not reach here");
        } catch (AbstractMainMemory.InvalidAddressException e) {
            // pass
        }

        // put 6 bytes into memory start from index 0, which exceed the byte capacity of memory
        try {
            memory.set(0, new byte[]{b1,b2,b3,b1,b2,b3});
            fail("should not reach here");
        } catch (AbstractMainMemory.InvalidAddressException e) {
            // pass
        }

        // put 2 byte into memory start from index 4, which exceed the byte capacity of memory
        try {
            memory.set(4, new byte[]{b4,b1});
            fail("should not reach here");
        } catch (AbstractMainMemory.InvalidAddressException e) {
            // pass
        }



        // test get 6 bytes from index 1, which exceed the byte capacity of memory
        try {
            assertArrayEquals(new byte[]{b0,b0,b0,b0}, memory.get(0, 6));
            fail("should not reach here");
        } catch (AbstractMainMemory.InvalidAddressException e) {
            // pass
        }

        // test get 1 byte from index 7, which exceed the byte capacity of memory
        try {
            assertArrayEquals(new byte[]{b0}, memory.get(7, 1));
            fail("should not reach here");
        } catch (AbstractMainMemory.InvalidAddressException e) {
            // pass
        }

        // test get 2 bytes from index 4, which exceed the byte capacity of memory
        try {
            assertArrayEquals(new byte[]{b0,b0}, memory.get(4, 2));
            fail("should not reach here");
        } catch (AbstractMainMemory.InvalidAddressException e) {
            // pass
        }
    }

    @Test
    void addition() {
        long x;
        int b = 3;
        x = b;
        int a = ~b;
        assertTrue((int) x == b);
        assertEquals(-4, a);
        int int1 = 0xf8;
        assertEquals(248, int1);
        assertEquals(-8, (byte) int1);

//        int inta1 = 0xe;
//        int inta2 = 0xf;
//        int inta3 = (((byte) inta1) << 4) + (inta2 & 0x0f);
//        assertEquals(-17, inta3);
        int inta4 = (0- (-17));
        assertEquals(17, inta4);
    }
}
