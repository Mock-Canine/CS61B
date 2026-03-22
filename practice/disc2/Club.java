package disc2;

import java.util.*;

public class Club {
    public boolean test = false;
    public Map<Student, Country> countryMap;
    public Club(Map<Student, Country> c) {
        countryMap = c;
    }
    public static Map<Country, Integer> countByCountry(List<Club> allClubs) {
        Map<Country, Integer> counts = new HashMap<>();
        Set<Student> allStudents = new HashSet<>();
        for (Club club : allClubs) {
            for (Student s : club.countryMap.keySet()) {
                Country c = club.countryMap.get(s);
                if (!counts.containsKey(c)) {
                    counts.put(c, 1);
                } else if (!allStudents.contains(s)){
                    counts.put(c, counts.get(c) + 1);
                }
            }
            allStudents.addAll(club.countryMap.keySet());
        }
//  Counts the student number, so check if the same person has been in the counts is better
//        for (Club club : allClubs) {
//            for (Student s : club.countryMap.keySet()) {
//                Country c = club.countryMap.get(s);
//                if (!allStudents.contains(s)) {
//                    if (!counts.containsKey(c)) {
//                        counts.put(c, 1);
//                    } else {
//                        counts.put(c, counts.get(c) + 1);
//                    }
//                    allStudents.add(s);
//                }
//            }
//        }
        return counts;
    }

    static void main() {
        // Pay attention to the identity, one instance, one identity
        Country China = new Country("China");
        Country USA = new Country("USA");
        Country Scot = new Country("Scotland");
        Student jack = new Student("jack");
        Club c1 = new Club(Map.of(jack, Scot,
                new Student("nita"), China,
                new Student("josh"), USA));
        Club c2 = new Club(Map.of(jack, Scot,
                new Student("poco"), Scot));
        List<Club> L = List.of(c1, c2);
        Map<Country, Integer> counts = Club.countByCountry(L);
    }
}

