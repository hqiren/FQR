import { Text, View, Dimensions, StyleSheet, FlatList, TouchableOpacity } from 'react-native';
import { PieChart } from 'react-native-gifted-charts';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Ionicons } from '@expo/vector-icons';
import { Checkbox } from 'expo-checkbox';
import { router } from 'expo-router';
import { useToDo } from '../context/ToDoContext';

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
const today = new Date().toLocaleDateString('en-US', {
  weekday: 'long',
  year: 'numeric',
  month: 'long',
  day: 'numeric'
});

type ToDoType = {
  id: number;
  task: string;
  isDone: boolean;
};

const palette = [
  '#1877F2', '#E1306C', '#FF4500', '#FFD700',
  '#00C49F', '#A855F7', '#FF6B6B', '#43AA8B'
];

// Pie Chart data
const mockData = [
  { value: 30, text: 'Facebook' },
  { value: 45, text: 'Instagram' },
  { value: 50, text: 'Reddit' },
  { value: 20, text: 'Mobile Legends' },
].map((item, index) => ({
  ...item,
  color: palette[index % palette.length]
}));

// Styles
const styles = StyleSheet.create({
  container: {
    flex: 1,
    flexDirection: 'column',
    justifyContent: 'space-between'
  },
  headerText: {
    fontWeight: 'bold',
    fontSize: 20,
    fontFamily: 'Georgia',
    textAlign: 'center'
  },
  date: {
    marginBottom: 5,
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
    gap: 4,
    alignItems: 'center',
  },
  addButton: {
    flexDirection: 'row', 
    justifyContent: 'center', 
    alignItems: 'center', 
    paddingVertical: 8
  },
  qotd: {
    fontFamily: 'Copperplate',
    fontSize: 18,
    textAlign: 'center',
  },
  menuButton: {
    justifyContent: 'flex-start'
  }
}
)

export default function HomeScreen() {
  const { toDoData } = useToDo();

  return (
    <SafeAreaView style={styles.container}>
      {/* Header Text above Pie Chart */}
      <Text style={styles.headerText}>Today's Screen Time</Text>
      <Text style={styles.date}>{today}</Text>

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

      <View style={{ flex: 1, justifyContent: 'space-evenly' }}>
      {/* To Do List */}
      <View style={{flex: 1, flexDirection: 'column'}}>
        <View style={styles.addButton}>
          <Text>Upcoming Activities</Text>
          <TouchableOpacity onPress={() => router.push('/goals')}>
            <Ionicons name='add-circle-outline' size={24} color='black' />
          </TouchableOpacity>
        </View>
      <FlatList 
        data={toDoData.slice(0, 3)}
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
        </View>

      {/* Quote of the Day */}
      <Text style={styles.qotd}>QOTD</Text>
      </View>

      {/* Menu Button */}
      <View style={styles.menuButton}>
        <TouchableOpacity onPress={() => alert("Clicked!")}>
          <Ionicons name='menu' size={40} color='black' />
        </TouchableOpacity>
      </View>
      

    </SafeAreaView>
  );
}