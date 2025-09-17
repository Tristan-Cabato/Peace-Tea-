// Delete
DROP USER '1000x';

// Test Cases
    // Update
        UPDATE students 
        SET Name = 'yy', Contact = 2
        WHERE id = 1004;
    // Insert
        INSERT INTO students VALUES (1004, 'x', null, 3, null, 3);