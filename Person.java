import java.util.HashMap;
import java.util.Date;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;


public class Person {

    private String personID;   
    private String firstName; 
    private String lastName; 
    private String address; 
    private String birthdate;
    private HashMap<Date, Integer> demeritPoints; // A variable that holds the demerit points with the offense day 
    private boolean isSuspended;

    public boolean addPerson() {
        //TODO: This method adds information about a person to a TXT file.
        //Condition 1: PersonID should be exactly 10 characters long;
        if (!validatePersonID(personID)) {
                return false;
        }
        //Condition 2: The address of the Person should follow the following format: Street Number|Street|City|State|Country.
        if (!validateAddress(address)) {
            return false;
        }
        //Condition 3: The format of the birth date of the person should follow the following format: DD-MM-YYYY. Example: 15-11-1990
        if (!validateBirthdate(birthdate)) {
            return false;
        }
        //Otherwise, the information should not be inserted into the TXT file, and the addPerson function should return false. 
        String record = String.join("|",
            personID,
            firstName,
            lastName,
            address,
            birthdate
        );
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("persons.txt", true))) {
            writer.write(record);
            writer.newLine();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    //the first two characters should be numbers between 2 and 9, there should be at least two special characters between characters 3 and 8, //and the last two characters should be upper case letters (A - 2). Example: "S6s_dSfAB"
    private boolean validatePersonID(String id) {
        if (id == null || id.length() != 10) return false;
        if (!id.substring(0, 2).matches("[2-9]{2}")) return false;
        if (!id.substring(8).matches("[A-Z]{2}")) return false;
        String middle = id.substring(2, 8);
        long specialCount = middle.chars()
            .filter(ch -> !Character.isLetterOrDigit(ch))
            .count();
        return specialCount >= 2;
    }
    
    //The State should be only Victoria. Example: 32|Highland Street Melbourne|Victoria (Australia.
    private boolean validateAddress(String addr) {
        if (addr == null) return false;
        String[] parts = addr.split("\\|");
        if (parts.length != 5) return false;
        String state = parts[3].trim();
        return "Victoria".equals(state);
    } 
      
    //Instruction: If the Person's information meets the above conditions and any other conditions you may want to consider, //the information should be inserted into a TXT file, and the addPerson function should return true.
    private boolean validateBirthdate(String bday) {
        if (bday == null) return false;
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        df.setLenient(false);
        try {
            df.parse(bday);
            return true;
        } catch (ParseException e) {
            return false;
        }    
    }
    
    public boolean updatePersonalDetails(String newFirstName, String newLastName, String newAddress, String newBirthdate) {
        //TODO: This method allows updating a given person's ID, firstName, lastName, address and birthday in a TXT file.
        //Changing personal details will not affect their demerit points or the suspension status.
        // All relevant conditions discussed for the addPerson function also need to be considered and checked in the updatePersonalDetails function.
        
        File inputFile = new File("persons.txt");
        File tempFile = new File("persons_temp.txt");
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        df.setLenient(false);
        boolean updated = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length != 5) {
                    writer.write(line);
                    writer.newLine();
                    continue;
                }

                String currentID = parts[0];
                String currentFirst = parts[1];
                String currentLast = parts[2];
                String currentAddress = parts[3];
                String currentBirthdate = parts[4];

                if (currentID.equals(this.personID)) {
                    int age = calculateAge(currentBirthdate);

                    boolean birthChanged = !currentBirthdate.equals(newBirthdate);
                    boolean nameChanged = !currentFirst.equals(newFirstName) || !currentLast.equals(newLastName);
                    boolean addressChanged = !currentAddress.equals(newAddress);

                    //Condition 1: If a person is under 18, their address cannot be Changed.
                    if (age < 18 && addressChanged) {
                        return false;
                    }

                    //Condition 2: If a person's birthday is going to be changed, then no other personal detail 
                    //(i.e, person's ID, firstName, LastName, address) can be changed.
                    if (birthChanged && (nameChanged || addressChanged)) {
                        return false;
                    }

                    //Condition 3: If the first character/digit of a person's ID is an even number, then their ID cannot be changed.
                    //We are not changing ID here, just checking it starts with an even number
                    char firstChar = personID.charAt(0);
                    if (Character.isDigit(firstChar) && (firstChar - '0') % 2 == 0) {
                        // ID is even, not changed — okay
                    }

                    // Write the updated record
                    String updatedLine = String.join("|", personID, newFirstName, newLastName, newAddress, newBirthdate);
                    writer.write(updatedLine);
                    updated = true;
                } else {
                    writer.write(line);
                }
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        if (!inputFile.delete()) return false;
        if (!tempFile.renameTo(inputFile)) return false;

        //Instruction: If the Person's updated information meets the below conditions and any other conditions you may want to consider,
        //the Person's information should be updated in the TXT file with the updated information, and the updatePersonalDetails function should return true.
        //Otherwise, the Person's information should not be updated in the TXT file, and the updatePersonalDetails function should return false.
        return updated;
    }

    // Helper function to calculate age in years from DD-MM-YYYY format
    private int calculateAge(String bday) {
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        df.setLenient(false);
        try {
            Date birth = df.parse(bday);
            Date today = new Date();
            long ageInMillis = today.getTime() - birth.getTime();
            long millisInYear = 1000L * 60 * 60 * 24 * 365;
            return (int)(ageInMillis / millisInYear);
        } catch (ParseException e) {
            return -1;
        }
    }

    public String addDemeritPoints() {
        //TODO: This method adds demerit points for a given person in a TXT file.
        //Condition 1: The format of the date of the offense should follow the following format: DD-MM-YYYY. Example: 15-11-1990
        //Condition 2: The demerit points must be a whole number between 1-6
        //Condition 3: If the person is under 21, the isSuspended variable should be set to true if the total demerit points within two years exceed 6.
        //If the person is over 21, the isSuspended variable should be set to true if the total demerit points within two years exceed 12.
        //Instruction: If the above condiations and any other conditions you may want to consider are net, the demerit points for a person should be inserted into the TXT file, //and the addDemeritPoints function should return "Sucess". Otherwise, the addDemeritPoints function should return Failed.
        return "Sucess";
    }
    
}    

