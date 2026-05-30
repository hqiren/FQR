import { Text, View, Dimensions, StyleSheet, FlatList, TouchableOpacity } from 'react-native';
import { PieChart } from 'react-native-gifted-charts';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Ionicons } from '@expo/vector-icons';
import { Checkbox } from 'expo-checkbox';

const chartConfig = {
  backgroundGradientFrom: "#1E2923",
  backgroundGradientFromOpacity: 0,
  backgroundGradientTo: "#08130D",
  backgroundGradientToOpacity: 0.5,
  color: (opacity = 1) => `rgba(26, 255, 146, ${opacity})`,
  strokeWidth: 2, // optional, default 3
  barPercentage: 0.5,
  useShadowColorFromDataset: false // optional
};

const screenWidth = Dimensions.get("window").width;
const screenLength = Dimensions.get("window").height;

// Time Spent Data
const mockData = [{value: 30, text: 'Facebook'}, {value: 45, text: 'Instagram'}, {value: 50, text: 'Reddit'
}, {value: 20, text: 'Mobile Legends'}];

// To Do List data
const toDoList = [
  {
    id: 1,
    task: "Task 1",
    isDone: true
  },
  {
    id: 2,
    task: "Task 2",
    isDone: false
  },
  {
    id: 3,
    task: "Task 3",
    isDone: false
  }
]

// Styles
const styles = StyleSheet.create({
  container: {
    flex: 1
  },
  headerText: {
    fontWeight: 'bold',
    fontSize: 20,
    fontFamily: 'Georgia',
    textAlign: 'center'
  },
  otherText: {
    fontFamily: 'Georgia',
    textAlign: 'center',
    fontSize: 16
  },
  pieChart: {
    alignItems: 'center'
  },
  legend: {
    marginBottom: 10
  },
  toDoContainer: {
    backgroundColor: '#fff',
    padding: 16,
    borderRadius: 16,
    marginBottom: 10,
  },
  toDoInfoContainer: {
    flexDirection: 'row',
    gap: 10,
    alignItems: 'center',
  },
  qotd: {
    fontFamily: 'Copperplate',
    fontSize: 18,
    textAlign: 'center',
    marginBottom: 25
  },
  menuButton: {
    justifyContent: 'flex-end'
  }
}
)

export default function HomeScreen() {
  return (
    <SafeAreaView style={styles.container}>
      {/* Header Text above Pie Chart */}
      <Text style={styles.headerText}>Today's Screen Time</Text>
      <Text style={styles.otherText}>Date</Text>

      {/* Pie Chart */}
      <View style={styles.pieChart}>
        <PieChart
              radius={150}
              textSize={20}
              data={mockData}
              showValuesAsLabels
        />
      </View>

      {/* Legend */}
        <View style={styles.legend}>
          {mockData.map((item) => (
            <View key={item.text} style={{ flexDirection: 'column', alignItems: 'center', gap: 2 }}>
              <View style={{ width: 12, height: 12, borderRadius: 6}} />
              <Text>{item.text}: {item.value}h</Text>
            </View>
          ))}
        </View>

      {/* To Do List */}
      <FlatList 
        data ={toDoList} 
        keyExtractor={(item) => item.id.toString()}
        renderItem={({item}) => 
          <View style={styles.toDoContainer}>
            <View style={styles.toDoInfoContainer}>
              <Checkbox value={item.isDone}></Checkbox>
              <Text style={item.isDone && { textDecorationLine: "line-through"}}>
                {item.task}
              </Text>
            </View>
          </View>
        }
        />

      {/* Quote of the Day */}
      <Text style={styles.qotd}>QOTD</Text>

      {/* Menu Button */}
      <View style={styles.menuButton}>
        <TouchableOpacity onPress={() => alert("Clicked!")}>
          <Ionicons name='menu' size={40} color='black' />
        </TouchableOpacity>
      </View>
      
    </SafeAreaView>
  );
}